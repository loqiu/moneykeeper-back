package com.loqiu.moneykeeper.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loqiu.moneykeeper.event.ExportJobEvent;
import com.loqiu.moneykeeper.service.ExportJobConsumerState;
import com.loqiu.moneykeeper.service.ExportJobEventHandler;
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
public class ExportJobKafkaConsumer implements ConsumerSeekAware {

    private static final Logger logger = LogManager.getLogger(ExportJobKafkaConsumer.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ExportJobEventHandler exportJobEventHandler;

    @Autowired
    private ExportJobConsumerState exportJobConsumerState;

    @KafkaListener(topics = "${app.kafka.export-job-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(ConsumerRecord<String, String> record) {
        try {
            ExportJobEvent event = objectMapper.readValue(record.value(), ExportJobEvent.class);
            exportJobEventHandler.handle(event);
            logger.info("Consumed export job event from Kafka - topic: {}, key: {}, eventId: {}",
                    record.topic(), record.key(), event.getEventId());
        } catch (Exception ex) {
            logger.error("Failed to consume export job event from Kafka - topic: {}, key: {}",
                    record.topic(), record.key(), ex);
        }
    }

    @Override
    public void onPartitionsAssigned(Map<TopicPartition, Long> assignments, ConsumerSeekCallback callback) {
        exportJobConsumerState.markReady();
        logger.info("Export job Kafka consumer is ready - assignments: {}", assignments.keySet());
    }
}
