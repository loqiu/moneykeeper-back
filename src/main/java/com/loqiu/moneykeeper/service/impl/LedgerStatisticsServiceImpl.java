package com.loqiu.moneykeeper.service.impl;

import com.loqiu.moneykeeper.dto.LedgerStatisticsBucketDTO;
import com.loqiu.moneykeeper.dto.LedgerStatisticsCategoryDTO;
import com.loqiu.moneykeeper.dto.LedgerStatisticsDTO;
import com.loqiu.moneykeeper.dto.MoneyKeeperDTO;
import com.loqiu.moneykeeper.exception.BadRequestException;
import com.loqiu.moneykeeper.service.LedgerStatisticsService;
import com.loqiu.moneykeeper.service.LedgerService;
import com.loqiu.moneykeeper.service.MoneyKeeperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class LedgerStatisticsServiceImpl implements LedgerStatisticsService {

    private static final String PERIOD_WEEK = "week";
    private static final String PERIOD_MONTH = "month";
    private static final String PERIOD_YEAR = "year";
    private static final String TYPE_INCOME = "income";
    private static final String TYPE_EXPENSE = "expense";
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final DateTimeFormatter DAY_LABEL_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");
    private static final DateTimeFormatter MONTH_LABEL_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    @Autowired
    private MoneyKeeperService moneyKeeperService;

    @Autowired
    private LedgerService ledgerService;

    @Override
    public LedgerStatisticsDTO getStatistics(Long ledgerId, String period, LocalDate anchorDate, Long userId) {
        ledgerService.requireLedger(ledgerId);
        if (userId != null && !ledgerService.hasActiveMembership(ledgerId, userId)) {
            throw new BadRequestException("Target user is not an active member of this ledger");
        }

        String normalizedPeriod = normalizePeriod(period);
        LocalDate effectiveAnchorDate = anchorDate == null ? LocalDate.now() : anchorDate;
        PeriodWindow currentWindow = resolveWindow(normalizedPeriod, effectiveAnchorDate);
        PeriodWindow previousWindow = resolvePreviousWindow(normalizedPeriod, currentWindow);

        List<MoneyKeeperDTO> currentRecords = moneyKeeperService.getAllLedgerRecordsWithCategoryName(
                ledgerId,
                userId,
                currentWindow.startDate(),
                currentWindow.endDate()
        );
        List<MoneyKeeperDTO> previousRecords = moneyKeeperService.getAllLedgerRecordsWithCategoryName(
                ledgerId,
                userId,
                previousWindow.startDate(),
                previousWindow.endDate()
        );

        AggregateSummary currentSummary = summarize(currentRecords);
        AggregateSummary previousSummary = summarize(previousRecords);

        return LedgerStatisticsDTO.builder()
                .ledgerId(ledgerId)
                .userId(userId)
                .period(normalizedPeriod)
                .bucketGranularity(resolveBucketGranularity(normalizedPeriod))
                .anchorDate(effectiveAnchorDate)
                .startDate(currentWindow.startDate())
                .endDate(currentWindow.endDate())
                .previousStartDate(previousWindow.startDate())
                .previousEndDate(previousWindow.endDate())
                .totalIncome(currentSummary.totalIncome())
                .totalExpense(currentSummary.totalExpense())
                .balance(currentSummary.balance())
                .recordCount(currentSummary.recordCount())
                .incomeRecordCount(currentSummary.incomeRecordCount())
                .expenseRecordCount(currentSummary.expenseRecordCount())
                .incomeDelta(scaleAmount(currentSummary.totalIncome().subtract(previousSummary.totalIncome())))
                .expenseDelta(scaleAmount(currentSummary.totalExpense().subtract(previousSummary.totalExpense())))
                .balanceDelta(scaleAmount(currentSummary.balance().subtract(previousSummary.balance())))
                .incomeChangePercentage(calculateChangePercentage(currentSummary.totalIncome(), previousSummary.totalIncome()))
                .expenseChangePercentage(calculateChangePercentage(currentSummary.totalExpense(), previousSummary.totalExpense()))
                .balanceChangePercentage(calculateChangePercentage(currentSummary.balance(), previousSummary.balance()))
                .buckets(buildBuckets(normalizedPeriod, currentWindow, currentRecords))
                .expenseCategories(buildCategoryBreakdown(currentRecords, TYPE_EXPENSE, currentSummary.totalExpense()))
                .incomeCategories(buildCategoryBreakdown(currentRecords, TYPE_INCOME, currentSummary.totalIncome()))
                .build();
    }

    private String normalizePeriod(String period) {
        if (!StringUtils.hasText(period)) {
            return PERIOD_MONTH;
        }
        String normalized = period.trim().toLowerCase(Locale.ROOT);
        if (!List.of(PERIOD_WEEK, PERIOD_MONTH, PERIOD_YEAR).contains(normalized)) {
            throw new BadRequestException("Period must be one of: week, month, year");
        }
        return normalized;
    }

    private PeriodWindow resolveWindow(String period, LocalDate anchorDate) {
        if (PERIOD_WEEK.equals(period)) {
            LocalDate startDate = anchorDate.with(DayOfWeek.MONDAY);
            return new PeriodWindow(startDate, startDate.plusDays(6));
        }
        if (PERIOD_YEAR.equals(period)) {
            LocalDate startDate = LocalDate.of(anchorDate.getYear(), 1, 1);
            return new PeriodWindow(startDate, LocalDate.of(anchorDate.getYear(), 12, 31));
        }
        YearMonth yearMonth = YearMonth.from(anchorDate);
        return new PeriodWindow(yearMonth.atDay(1), yearMonth.atEndOfMonth());
    }

    private PeriodWindow resolvePreviousWindow(String period, PeriodWindow currentWindow) {
        if (PERIOD_WEEK.equals(period)) {
            return new PeriodWindow(currentWindow.startDate().minusDays(7), currentWindow.endDate().minusDays(7));
        }
        if (PERIOD_YEAR.equals(period)) {
            return new PeriodWindow(currentWindow.startDate().minusYears(1), currentWindow.endDate().minusYears(1));
        }
        LocalDate previousAnchor = currentWindow.startDate().minusMonths(1);
        YearMonth previousMonth = YearMonth.from(previousAnchor);
        return new PeriodWindow(previousMonth.atDay(1), previousMonth.atEndOfMonth());
    }

    private String resolveBucketGranularity(String period) {
        return PERIOD_YEAR.equals(period) ? "month" : "day";
    }

    private AggregateSummary summarize(List<MoneyKeeperDTO> records) {
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        int incomeRecordCount = 0;
        int expenseRecordCount = 0;

        for (MoneyKeeperDTO record : records) {
            BigDecimal amount = defaultAmount(record.getAmount());
            if (TYPE_INCOME.equals(record.getType())) {
                totalIncome = totalIncome.add(amount);
                incomeRecordCount++;
            } else if (TYPE_EXPENSE.equals(record.getType())) {
                totalExpense = totalExpense.add(amount);
                expenseRecordCount++;
            }
        }

        return new AggregateSummary(
                scaleAmount(totalIncome),
                scaleAmount(totalExpense),
                scaleAmount(totalIncome.subtract(totalExpense)),
                records.size(),
                incomeRecordCount,
                expenseRecordCount
        );
    }

    private List<LedgerStatisticsBucketDTO> buildBuckets(String period, PeriodWindow window, List<MoneyKeeperDTO> records) {
        Map<String, List<MoneyKeeperDTO>> recordsByBucket = new LinkedHashMap<>();
        if (PERIOD_YEAR.equals(period)) {
            for (YearMonth monthCursor = YearMonth.from(window.startDate());
                 !monthCursor.isAfter(YearMonth.from(window.endDate()));
                 monthCursor = monthCursor.plusMonths(1)) {
                recordsByBucket.put(monthCursor.toString(), new ArrayList<>());
            }
            for (MoneyKeeperDTO record : records) {
                if (record.getTransactionDate() != null) {
                    String bucketKey = YearMonth.from(record.getTransactionDate()).toString();
                    recordsByBucket.computeIfAbsent(bucketKey, ignored -> new ArrayList<>()).add(record);
                }
            }
            return recordsByBucket.entrySet().stream()
                    .map(entry -> {
                        YearMonth yearMonth = YearMonth.parse(entry.getKey());
                        AggregateSummary summary = summarize(entry.getValue());
                        return LedgerStatisticsBucketDTO.builder()
                                .bucketKey(entry.getKey())
                                .label(yearMonth.format(MONTH_LABEL_FORMATTER))
                                .startDate(yearMonth.atDay(1))
                                .endDate(yearMonth.atEndOfMonth())
                                .totalIncome(summary.totalIncome())
                                .totalExpense(summary.totalExpense())
                                .balance(summary.balance())
                                .recordCount(summary.recordCount())
                                .build();
                    })
                    .toList();
        }

        for (LocalDate dateCursor = window.startDate(); !dateCursor.isAfter(window.endDate()); dateCursor = dateCursor.plusDays(1)) {
            recordsByBucket.put(dateCursor.toString(), new ArrayList<>());
        }
        for (MoneyKeeperDTO record : records) {
            if (record.getTransactionDate() != null) {
                String bucketKey = record.getTransactionDate().toString();
                recordsByBucket.computeIfAbsent(bucketKey, ignored -> new ArrayList<>()).add(record);
            }
        }
        return recordsByBucket.entrySet().stream()
                .map(entry -> {
                    LocalDate date = LocalDate.parse(entry.getKey());
                    AggregateSummary summary = summarize(entry.getValue());
                    return LedgerStatisticsBucketDTO.builder()
                            .bucketKey(entry.getKey())
                            .label(date.format(DAY_LABEL_FORMATTER))
                            .startDate(date)
                            .endDate(date)
                            .totalIncome(summary.totalIncome())
                            .totalExpense(summary.totalExpense())
                            .balance(summary.balance())
                            .recordCount(summary.recordCount())
                            .build();
                })
                .toList();
    }

    private List<LedgerStatisticsCategoryDTO> buildCategoryBreakdown(List<MoneyKeeperDTO> records,
                                                                     String type,
                                                                     BigDecimal totalForType) {
        Map<Long, CategoryAggregate> aggregateByCategory = new LinkedHashMap<>();

        for (MoneyKeeperDTO record : records) {
            if (!type.equals(record.getType())) {
                continue;
            }
            Long categoryId = record.getCategoryId() == null ? -1L : record.getCategoryId();
            CategoryAggregate aggregate = aggregateByCategory.computeIfAbsent(categoryId, ignored -> new CategoryAggregate(
                    record.getCategoryId(),
                    StringUtils.hasText(record.getCategoryName()) ? record.getCategoryName() : "Uncategorized",
                    type,
                    BigDecimal.ZERO,
                    0
            ));
            aggregate.totalAmount = aggregate.totalAmount.add(defaultAmount(record.getAmount()));
            aggregate.recordCount++;
        }

        return aggregateByCategory.values().stream()
                .map(aggregate -> LedgerStatisticsCategoryDTO.builder()
                        .categoryId(aggregate.categoryId)
                        .categoryName(aggregate.categoryName)
                        .type(aggregate.type)
                        .totalAmount(scaleAmount(aggregate.totalAmount))
                        .percentage(calculateRatioPercentage(aggregate.totalAmount, totalForType))
                        .recordCount(aggregate.recordCount)
                        .build())
                .sorted(Comparator.comparing(LedgerStatisticsCategoryDTO::getTotalAmount, Comparator.nullsLast(BigDecimal::compareTo)).reversed()
                        .thenComparing(item -> Objects.toString(item.getCategoryName(), "")))
                .collect(Collectors.toList());
    }

    private BigDecimal calculateChangePercentage(BigDecimal current, BigDecimal previous) {
        BigDecimal safeCurrent = defaultAmount(current);
        BigDecimal safePrevious = defaultAmount(previous);
        if (safePrevious.compareTo(BigDecimal.ZERO) == 0) {
            return safeCurrent.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : null;
        }
        BigDecimal delta = safeCurrent.subtract(safePrevious);
        return delta.divide(safePrevious.abs(), 4, RoundingMode.HALF_UP)
                .multiply(ONE_HUNDRED)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateRatioPercentage(BigDecimal amount, BigDecimal total) {
        BigDecimal safeAmount = defaultAmount(amount);
        BigDecimal safeTotal = defaultAmount(total);
        if (safeTotal.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return safeAmount.divide(safeTotal, 4, RoundingMode.HALF_UP)
                .multiply(ONE_HUNDRED)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal defaultAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal scaleAmount(BigDecimal value) {
        return defaultAmount(value).setScale(2, RoundingMode.HALF_UP);
    }

    private record PeriodWindow(LocalDate startDate, LocalDate endDate) {
    }

    private record AggregateSummary(BigDecimal totalIncome,
                                    BigDecimal totalExpense,
                                    BigDecimal balance,
                                    Integer recordCount,
                                    Integer incomeRecordCount,
                                    Integer expenseRecordCount) {
    }

    private static final class CategoryAggregate {
        private final Long categoryId;
        private final String categoryName;
        private final String type;
        private BigDecimal totalAmount;
        private int recordCount;

        private CategoryAggregate(Long categoryId, String categoryName, String type, BigDecimal totalAmount, int recordCount) {
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.type = type;
            this.totalAmount = totalAmount;
            this.recordCount = recordCount;
        }
    }
}
