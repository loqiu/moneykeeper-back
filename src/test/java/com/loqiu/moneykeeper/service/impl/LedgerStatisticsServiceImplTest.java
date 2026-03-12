package com.loqiu.moneykeeper.service.impl;

import com.loqiu.moneykeeper.dto.LedgerStatisticsDTO;
import com.loqiu.moneykeeper.dto.MoneyKeeperDTO;
import com.loqiu.moneykeeper.service.LedgerService;
import com.loqiu.moneykeeper.service.MoneyKeeperService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LedgerStatisticsServiceImplTest {

    @Mock
    private MoneyKeeperService moneyKeeperService;

    @Mock
    private LedgerService ledgerService;

    private LedgerStatisticsServiceImpl ledgerStatisticsService;

    @BeforeEach
    void setUp() {
        ledgerStatisticsService = new LedgerStatisticsServiceImpl();
        ReflectionTestUtils.setField(ledgerStatisticsService, "moneyKeeperService", moneyKeeperService);
        ReflectionTestUtils.setField(ledgerStatisticsService, "ledgerService", ledgerService);
    }

    @Test
    void getStatisticsShouldAggregateMonthlyLedgerData() {
        List<MoneyKeeperDTO> currentRecords = List.of(
                record(8L, "Food", "expense", "20.00", LocalDate.of(2026, 3, 1)),
                record(9L, "Coffee", "expense", "30.00", LocalDate.of(2026, 3, 5)),
                record(10L, "Salary", "income", "200.00", LocalDate.of(2026, 3, 10)),
                record(9L, "Coffee", "expense", "10.00", LocalDate.of(2026, 3, 10))
        );
        List<MoneyKeeperDTO> previousRecords = List.of(
                record(8L, "Food", "expense", "15.00", LocalDate.of(2026, 2, 2)),
                record(10L, "Salary", "income", "150.00", LocalDate.of(2026, 2, 15))
        );

        when(moneyKeeperService.getAllLedgerRecordsWithCategoryName(31L, null, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31)))
                .thenReturn(currentRecords);
        when(moneyKeeperService.getAllLedgerRecordsWithCategoryName(31L, null, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28)))
                .thenReturn(previousRecords);

        LedgerStatisticsDTO result = ledgerStatisticsService.getStatistics(31L, "month", LocalDate.of(2026, 3, 12), null);

        verify(ledgerService).requireLedger(31L);
        assertEquals(LocalDate.of(2026, 3, 1), result.getStartDate());
        assertEquals(LocalDate.of(2026, 3, 31), result.getEndDate());
        assertEquals(new BigDecimal("200.00"), result.getTotalIncome());
        assertEquals(new BigDecimal("60.00"), result.getTotalExpense());
        assertEquals(new BigDecimal("140.00"), result.getBalance());
        assertEquals(4, result.getRecordCount());
        assertEquals(1, result.getIncomeRecordCount());
        assertEquals(3, result.getExpenseRecordCount());
        assertEquals(new BigDecimal("50.00"), result.getIncomeDelta());
        assertEquals(new BigDecimal("45.00"), result.getExpenseDelta());
        assertEquals(new BigDecimal("5.00"), result.getBalanceDelta());
        assertEquals(new BigDecimal("33.33"), result.getIncomeChangePercentage());
        assertEquals(new BigDecimal("300.00"), result.getExpenseChangePercentage());
        assertEquals(new BigDecimal("3.70"), result.getBalanceChangePercentage());
        assertEquals(31, result.getBuckets().size());
        assertEquals("2026-03-10", result.getBuckets().get(9).getBucketKey());
        assertEquals(new BigDecimal("200.00"), result.getBuckets().get(9).getTotalIncome());
        assertEquals(new BigDecimal("10.00"), result.getBuckets().get(9).getTotalExpense());
        assertEquals(2, result.getBuckets().get(9).getRecordCount());
        assertEquals(2, result.getExpenseCategories().size());
        assertEquals("Coffee", result.getExpenseCategories().get(0).getCategoryName());
        assertEquals(new BigDecimal("40.00"), result.getExpenseCategories().get(0).getTotalAmount());
        assertEquals(new BigDecimal("66.67"), result.getExpenseCategories().get(0).getPercentage());
        assertEquals("Salary", result.getIncomeCategories().get(0).getCategoryName());
        assertEquals(new BigDecimal("100.00"), result.getIncomeCategories().get(0).getPercentage());
    }

    @Test
    void getStatisticsShouldReturnNullChangePercentageWhenPreviousPeriodIsZero() {
        when(moneyKeeperService.getAllLedgerRecordsWithCategoryName(31L, null, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)))
                .thenReturn(List.of(record(10L, "Salary", "income", "80.00", LocalDate.of(2026, 6, 1))));
        when(moneyKeeperService.getAllLedgerRecordsWithCategoryName(31L, null, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31)))
                .thenReturn(List.of());

        LedgerStatisticsDTO result = ledgerStatisticsService.getStatistics(31L, "year", LocalDate.of(2026, 6, 10), null);

        assertNull(result.getIncomeChangePercentage());
        assertEquals(new BigDecimal("0.00"), result.getExpenseChangePercentage());
        assertEquals(12, result.getBuckets().size());
        assertEquals("2026-06", result.getBuckets().get(5).getBucketKey());
    }

    private MoneyKeeperDTO record(Long categoryId,
                                  String categoryName,
                                  String type,
                                  String amount,
                                  LocalDate transactionDate) {
        return MoneyKeeperDTO.builder()
                .categoryId(categoryId)
                .categoryName(categoryName)
                .type(type)
                .amount(new BigDecimal(amount))
                .transactionDate(transactionDate)
                .build();
    }
}
