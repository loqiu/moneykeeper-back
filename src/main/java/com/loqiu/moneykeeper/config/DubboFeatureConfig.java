package com.loqiu.moneykeeper.config;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableDubbo
@ConditionalOnProperty(prefix = "app.dubbo", name = "enabled", havingValue = "true", matchIfMissing = false)
public class DubboFeatureConfig {
}