package com.loqiu.moneykeeper.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentBillingPortalSessionRequest {
    private String returnUrl;
}