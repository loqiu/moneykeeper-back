package com.loqiu.moneykeeper.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Data
@Component
@ConfigurationProperties(prefix = "app.payment")
public class PaymentProperties {
    private boolean enabled = false;
    private String provider = "stripe";
    private String defaultCurrency = "usd";
    private String secretKey = "";
    private String webhookSecret = "";
    private String billingPortalReturnUrl = "";

    public boolean hasSecretKey() {
        return StringUtils.hasText(secretKey);
    }

    public boolean hasWebhookSecret() {
        return StringUtils.hasText(webhookSecret);
    }

    public boolean hasBillingPortalReturnUrl() {
        return StringUtils.hasText(billingPortalReturnUrl);
    }
}