package com.loqiu.moneykeeper.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loqiu.moneykeeper.event.RecordChangedEvent;
import com.loqiu.moneykeeper.service.RecordEventHandler;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "true")
public class RecordEventKafkaConsumer {

    private static final Logger logger = LogManager.getLogger(RecordEventKafkaConsumer.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RecordEventHandler recordEventHandler;

    @KafkaListener(topics = "${app.kafka.record-event-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(ConsumerRecord<String, String> record) {
        try {
            RecordChangedEvent event = objectMapper.readValue(record.value(), RecordChangedEvent.class);
            recordEventHandler.handle(event);
            logger.info("Consumed record change event from Kafka - topic: {}, key: {}, eventId: {}",
                    record.topic(), record.key(), event.getEventId());
        } catch (Exception ex) {
            logger.error("Failed to consume record change event from Kafka - topic: {}, key: {}",
                    record.topic(), record.key(), ex);
        }
    }
}
