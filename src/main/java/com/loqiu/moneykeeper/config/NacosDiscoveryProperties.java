package com.loqiu.moneykeeper.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.nacos.discovery")
public class NacosDiscoveryProperties {
    private boolean enabled = false;
    private String serverAddr = "localhost:8848";
}
