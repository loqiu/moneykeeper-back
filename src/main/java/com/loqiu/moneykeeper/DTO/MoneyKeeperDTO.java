package com.loqiu.moneykeeper.DTO;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class MoneyKeeperDTO {
    private Long id;
    private Long userId;
    private Long categoryId;
    private LocalDateTime updatedAt;
    private LocalDateTime transactionDate;
    private String categoryName;
    private String type;
    private BigDecimal amount;
    private String notes;
}
