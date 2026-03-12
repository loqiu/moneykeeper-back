package com.loqiu.moneykeeper.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LedgerStatisticsDTO {
    private Long ledgerId;
    private Long userId;
    private String period;
    private String bucketGranularity;
    private LocalDate anchorDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate previousStartDate;
    private LocalDate previousEndDate;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal balance;
    private Integer recordCount;
    private Integer incomeRecordCount;
    private Integer expenseRecordCount;
    private BigDecimal incomeDelta;
    private BigDecimal expenseDelta;
    private BigDecimal balanceDelta;
    private BigDecimal incomeChangePercentage;
    private BigDecimal expenseChangePercentage;
    private BigDecimal balanceChangePercentage;
    private List<LedgerStatisticsBucketDTO> buckets;
    private List<LedgerStatisticsCategoryDTO> expenseCategories;
    private List<LedgerStatisticsCategoryDTO> incomeCategories;
}
