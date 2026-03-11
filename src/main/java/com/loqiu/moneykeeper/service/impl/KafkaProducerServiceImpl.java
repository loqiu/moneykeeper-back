package com.loqiu.moneykeeper.service.impl;

import com.loqiu.moneykeeper.config.KafkaFeatureProperties;
import com.loqiu.moneykeeper.constant.KafkaTopicConstant;
import com.loqiu.moneykeeper.service.KafkaProducerService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class KafkaProducerServiceImpl implements KafkaProducerService {

    private static final Logger logger = LogManager.getLogger(KafkaProducerServiceImpl.class);

    @Autowired
    private ObjectProvider<KafkaTemplate<String, String>> kafkaTemplateProvider;

    @Autowired
    private KafkaFeatureProperties kafkaFeatureProperties;

    @Override
    public void sendMessage(String topic, String key, String value) {
        if (!isEnabled()) {
            throw new IllegalStateException("Kafka module is disabled");
        }
        String resolvedTopic = StringUtils.hasText(topic) ? topic.trim() : KafkaTopicConstant.QUICKSTART_EVENTS;
        logger.info("Sending message to topic: {}, key: {}, value: {}", resolvedTopic, key, value);
        KafkaTemplate<String, String> kafkaTemplate = kafkaTemplateProvider.getIfAvailable();
        if (kafkaTemplate == null) {
            throw new IllegalStateException("Kafka template is not available");
        }
        kafkaTemplate.send(resolvedTopic, key, value);
    }

    @Override
    public boolean isEnabled() {
        return kafkaFeatureProperties.isEnabled() && kafkaTemplateProvider.getIfAvailable() != null;
    }
}
