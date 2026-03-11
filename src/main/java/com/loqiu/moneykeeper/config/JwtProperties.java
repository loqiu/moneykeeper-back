package com.loqiu.moneykeeper.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.security.jwt")
public class JwtProperties {
    private String secret = "moneykeeper_secret_key";
}
