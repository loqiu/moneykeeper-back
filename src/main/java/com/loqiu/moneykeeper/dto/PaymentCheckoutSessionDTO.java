package com.loqiu.moneykeeper.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCheckoutSessionDTO {
    private String orderNo;
    private String planCode;
    private String checkoutSessionId;
    private String checkoutUrl;
    private String status;
}