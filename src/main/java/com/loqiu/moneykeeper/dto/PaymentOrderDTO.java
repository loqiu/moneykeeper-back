package com.loqiu.moneykeeper.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentOrderDTO {
    private String orderNo;
    private String planCode;
    private String planName;
    private String orderType;
    private String status;
    private String currency;
    private Long amountMinor;
    private String stripeCheckoutSessionId;
    private String stripeInvoiceId;
    private String stripeSubscriptionId;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}