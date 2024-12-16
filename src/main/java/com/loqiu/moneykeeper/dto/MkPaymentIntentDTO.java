package com.loqiu.moneykeeper.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MkPaymentIntentDTO {
    private String id;
    private Long amount;
    private String currency;
    private String status;
    private String clientSecret;
    private String paymentMethod;
    private Map<String, String> metadata;
}
