package com.loqiu.moneykeeper.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetProgressDTO {
    private BigDecimal spentAmount;
    private BigDecimal remainingAmount;
    private BigDecimal usagePercentage;
    private boolean exceeded;
    private List<BigDecimal> triggeredThresholdPercentages;
}