package com.loqiu.moneykeeper.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BudgetRuleRequest {
    private BigDecimal thresholdPercentage;
    private Boolean enabled;
    private String notificationTitle;
    private String notificationMessage;
}