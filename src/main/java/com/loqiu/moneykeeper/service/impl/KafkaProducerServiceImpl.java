package com.loqiu.moneykeeper.service.impl;

import com.loqiu.moneykeeper.constant.KafkaTopicConstant;
import com.loqiu.moneykeeper.service.KafkaProducerService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerServiceImpl implements KafkaProducerService {

    private static final Logger logger = LogManager.getLogger(KafkaProducerServiceImpl.class);

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public void sendMessage(String topic, String key, String value) {
        logger.info("Sending message to topic: {}, key: {}, value: {}", topic, key, value);
        kafkaTemplate.send(KafkaTopicConstant.QUICKSTART_EVENTS, key, value);
    }

}
