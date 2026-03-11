package com.loqiu.moneykeeper.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.nacos.config")
public class NacosConfigProperties {
    private boolean enabled = false;
    private String serverAddr = "localhost:8848";
}
