package com.loqiu.moneykeeper.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class SseEmitterConfig {
    @Bean
    public ConcurrentHashMap<Long, SseEmitter> emitterMap() {
        return new ConcurrentHashMap<>();
    }
}
