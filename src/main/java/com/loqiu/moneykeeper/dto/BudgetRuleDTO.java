package com.loqiu.moneykeeper.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetRuleDTO {
    private Long id;
    private Long budgetId;
    private String ruleType;
    private BigDecimal thresholdPercentage;
    private boolean enabled;
    private String notificationTitle;
    private String notificationMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}