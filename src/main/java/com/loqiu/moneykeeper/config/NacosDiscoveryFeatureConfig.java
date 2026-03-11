package com.loqiu.moneykeeper.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableDiscoveryClient
@ConditionalOnProperty(prefix = "app.nacos.discovery", name = "enabled", havingValue = "true", matchIfMissing = false)
public class NacosDiscoveryFeatureConfig {
}
