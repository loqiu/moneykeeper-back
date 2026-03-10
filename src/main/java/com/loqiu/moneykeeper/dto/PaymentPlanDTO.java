package com.loqiu.moneykeeper.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentPlanDTO {
    private String code;
    private String name;
    private String description;
    private String currency;
    private Long amountMinor;
    private String billingInterval;
}