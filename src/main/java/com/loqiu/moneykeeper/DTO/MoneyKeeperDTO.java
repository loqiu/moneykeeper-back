package com.loqiu.moneykeeper.DTO;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class MoneyKeeperDTO {
    private Long id;
    private Long userId;
    private Long categoryId;
    private String categoryName;
    private String type;
    private BigDecimal amount;
    private String notes;
}
