package com.loqiu.moneykeeper.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loqiu.moneykeeper.config.KafkaFeatureProperties;
import com.loqiu.moneykeeper.entity.MoneyKeeper;
import com.loqiu.moneykeeper.event.RecordChangedEvent;
import com.loqiu.moneykeeper.service.KafkaProducerService;
import com.loqiu.moneykeeper.service.RecordEventConsumerState;
import com.loqiu.moneykeeper.service.RecordEventDispatcher;
import com.loqiu.moneykeeper.service.RecordEventHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RecordEventDispatcherImpl implements RecordEventDispatcher {

    private static final Logger logger = LogManager.getLogger(RecordEventDispatcherImpl.class);

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Autowired
    private KafkaFeatureProperties kafkaFeatureProperties;

    @Autowired
    private RecordEventHandler recordEventHandler;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RecordEventConsumerState recordEventConsumerState;

    @Override
    public void dispatchRecordCreated(Long ledgerId, MoneyKeeper currentRecord) {
        dispatch(RecordChangedEvent.created(ledgerId, currentRecord));
    }

    @Override
    public void dispatchRecordUpdated(Long ledgerId, MoneyKeeper previousRecord, MoneyKeeper currentRecord) {
        dispatch(RecordChangedEvent.updated(ledgerId, previousRecord, currentRecord));
    }

    @Override
    public void dispatchRecordDeleted(Long ledgerId, MoneyKeeper previousRecord) {
        dispatch(RecordChangedEvent.deleted(ledgerId, previousRecord));
    }

    private void dispatch(RecordChangedEvent event) {
        if (event == null) {
            return;
        }
        if (shouldPublishToKafka()) {
            try {
                kafkaProducerService.sendMessage(
                        kafkaFeatureProperties.getRecordEventTopic(),
                        buildPartitionKey(event),
                        objectMapper.writeValueAsString(event)
                );
                logger.info("Published record change event to Kafka - eventId: {}, type: {}, recordId: {}",
                        event.getEventId(), event.getChangeType(), event.getRecordId());
                return;
            } catch (JsonProcessingException ex) {
                logger.warn("Failed to serialize record change event - eventId: {}, message: {}",
                        event.getEventId(), ex.getMessage());
            } catch (RuntimeException ex) {
                logger.warn("Failed to publish record change event to Kafka - eventId: {}, message: {}",
                        event.getEventId(), ex.getMessage());
            }
        }
        recordEventHandler.handle(event);
    }

    private boolean shouldPublishToKafka() {
        return kafkaFeatureProperties.isEnabled()
                && kafkaFeatureProperties.isListenerAutoStartup()
                && recordEventConsumerState.isReady()
                && kafkaProducerService.isEnabled();
    }

    private String buildPartitionKey(RecordChangedEvent event) {
        return event.getLedgerId() + ":" + event.getRecordId();
    }
}
