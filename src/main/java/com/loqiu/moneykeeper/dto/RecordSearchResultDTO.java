package com.loqiu.moneykeeper.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordSearchResultDTO {
    private Long id;
    private Long userId;
    private Long categoryId;
    private String categoryName;
    private String type;
    private BigDecimal amount;
    private LocalDate transactionDate;
    private LocalDateTime updatedAt;
    private String notes;
    private Double score;
}