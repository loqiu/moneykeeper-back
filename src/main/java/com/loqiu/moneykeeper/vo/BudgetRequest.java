package com.loqiu.moneykeeper.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BudgetRequest {
    private String name;
    private Long categoryId;
    private String type;
    private BigDecimal amount;
    private Integer budgetYear;
    private Integer budgetMonth;
    private String notes;
}