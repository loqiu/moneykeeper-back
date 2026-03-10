package com.loqiu.moneykeeper.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MoneyKeeperUpdateRequest {
    private Long categoryId;
    private String type;
    private BigDecimal amount;
    private LocalDate transactionDate;
    private String notes;
}