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
public class PaymentSubscriptionDTO {
    private boolean active;
    private String status;
    private String planCode;
    private String planName;
    private String currency;
    private Long amountMinor;
    private String billingInterval;
    private LocalDateTime currentPeriodStart;
    private LocalDateTime currentPeriodEnd;
    private boolean cancelAtPeriodEnd;
    private LocalDateTime canceledAt;
    private String stripeSubscriptionId;

    public static PaymentSubscriptionDTO none() {
        return PaymentSubscriptionDTO.builder()
                .active(false)
                .status("none")
                .cancelAtPeriodEnd(false)
                .build();
    }
}