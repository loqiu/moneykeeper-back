package com.loqiu.moneykeeper.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.dubbo")
public class DubboModuleProperties {
    private boolean enabled = false;
    private String applicationName = "moneykeeper-back";
    private String registryAddress = "nacos://localhost:8848";
}
