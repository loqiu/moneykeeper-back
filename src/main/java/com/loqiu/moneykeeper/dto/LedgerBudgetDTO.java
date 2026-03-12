package com.loqiu.moneykeeper.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LedgerBudgetDTO {
    private Long id;
    private Long ledgerId;
    private Long createdByUserId;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String periodType;
    private Integer budgetYear;
    private Integer budgetMonth;
    private LocalDate startDate;
    private LocalDate endDate;
    private String type;
    private BigDecimal amount;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private BudgetProgressDTO progress;
    private List<BudgetRuleDTO> rules;
}