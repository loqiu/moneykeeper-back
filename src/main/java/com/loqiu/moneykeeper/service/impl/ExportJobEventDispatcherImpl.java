package com.loqiu.moneykeeper.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loqiu.moneykeeper.config.KafkaFeatureProperties;
import com.loqiu.moneykeeper.event.ExportJobEvent;
import com.loqiu.moneykeeper.service.ExportJobConsumerState;
import com.loqiu.moneykeeper.service.ExportJobEventDispatcher;
import com.loqiu.moneykeeper.service.KafkaProducerService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExportJobEventDispatcherImpl implements ExportJobEventDispatcher {

    private static final Logger logger = LogManager.getLogger(ExportJobEventDispatcherImpl.class);

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Autowired
    private KafkaFeatureProperties kafkaFeatureProperties;

    @Autowired
    private ExportJobConsumerState exportJobConsumerState;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void dispatchCreated(Long jobId, Long ledgerId, Long requestedByUserId) {
        ExportJobEvent event = ExportJobEvent.created(jobId, ledgerId, requestedByUserId);
        if (!shouldPublishToKafka()) {
            return;
        }
        try {
            kafkaProducerService.sendMessage(
                    kafkaFeatureProperties.getExportJobTopic(),
                    buildPartitionKey(event),
                    objectMapper.writeValueAsString(event)
            );
            logger.info("Published export job event to Kafka - eventId: {}, jobId: {}",
                    event.getEventId(), event.getJobId());
        } catch (JsonProcessingException ex) {
            logger.warn("Failed to serialize export job event - eventId: {}, message: {}",
                    event.getEventId(), ex.getMessage());
        } catch (RuntimeException ex) {
            logger.warn("Failed to publish export job event to Kafka - eventId: {}, message: {}",
                    event.getEventId(), ex.getMessage());
        }
    }

    private boolean shouldPublishToKafka() {
        return kafkaFeatureProperties.isEnabled()
                && kafkaFeatureProperties.isListenerAutoStartup()
                && exportJobConsumerState.isReady()
                && kafkaProducerService.isEnabled();
    }

    private String buildPartitionKey(ExportJobEvent event) {
        return event.getLedgerId() + ":" + event.getJobId();
    }
}
