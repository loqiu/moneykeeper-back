package com.loqiu.moneykeeper.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordSearchDocument {
    private Long recordId;
    private Long ledgerId;
    private Long userId;
    private Long categoryId;
    private String categoryName;
    private String type;
    private BigDecimal amount;
    private String transactionDate;
    private String updatedAt;
    private String notes;
}