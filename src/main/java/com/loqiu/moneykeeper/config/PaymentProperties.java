package com.loqiu.moneykeeper.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.payment")
public class PaymentProperties {
    private boolean enabled = false;
    private String provider = "stripe";
    private String defaultCurrency = "usd";
}