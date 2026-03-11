package com.loqiu.moneykeeper.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.kafka")
public class KafkaFeatureProperties {
    private boolean enabled = false;
    private String bootstrapServers = "localhost:9092";
    private String consumerGroupId = "Kafka-Mk-ConsumerGroup";
    private boolean listenerAutoStartup = false;
}
