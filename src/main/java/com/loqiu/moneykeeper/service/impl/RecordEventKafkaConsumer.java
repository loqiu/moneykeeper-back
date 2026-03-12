package com.loqiu.moneykeeper.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loqiu.moneykeeper.event.RecordChangedEvent;
import com.loqiu.moneykeeper.service.RecordEventConsumerState;
import com.loqiu.moneykeeper.service.RecordEventHandler;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.ConsumerSeekAware;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "true")
public class RecordEventKafkaConsumer implements ConsumerSeekAware {

    private static final Logger logger = LogManager.getLogger(RecordEventKafkaConsumer.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RecordEventHandler recordEventHandler;

    @Autowired
    private RecordEventConsumerState recordEventConsumerState;

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

    @Override
    public void onPartitionsAssigned(Map<TopicPartition, Long> assignments, ConsumerSeekCallback callback) {
        recordEventConsumerState.markReady();
        logger.info("Record event Kafka consumer is ready - assignments: {}", assignments.keySet());
    }
}
