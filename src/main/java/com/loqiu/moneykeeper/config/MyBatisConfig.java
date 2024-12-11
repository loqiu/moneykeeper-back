package com.loqiu.moneykeeper.config;

import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyBatisConfig {

    @PostConstruct
    public void init() {
        // 强制使用 Log4j2
        org.apache.ibatis.logging.LogFactory.useLog4J2Logging();
    }

    @Bean
    public ConfigurationCustomizer configurationCustomizer() {
        return configuration -> {
            configuration.setLogImpl(org.apache.ibatis.logging.log4j2.Log4j2Impl.class);
        };
    }
}
