package com.loqiu.moneykeeper.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loqiu.moneykeeper.config.KafkaFeatureProperties;
import com.loqiu.moneykeeper.entity.MoneyKeeper;
import com.loqiu.moneykeeper.service.KafkaProducerService;
import com.loqiu.moneykeeper.service.RecordEventConsumerState;
import com.loqiu.moneykeeper.service.RecordEventHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecordEventDispatcherImplTest {

    @Mock
    private KafkaProducerService kafkaProducerService;

    @Mock
    private RecordEventHandler recordEventHandler;

    @Mock
    private RecordEventConsumerState recordEventConsumerState;

    private RecordEventDispatcherImpl dispatcher;
    private KafkaFeatureProperties kafkaFeatureProperties;

    @BeforeEach
    void setUp() {
        dispatcher = new RecordEventDispatcherImpl();
        kafkaFeatureProperties = new KafkaFeatureProperties();
        ReflectionTestUtils.setField(dispatcher, "kafkaProducerService", kafkaProducerService);
        ReflectionTestUtils.setField(dispatcher, "kafkaFeatureProperties", kafkaFeatureProperties);
        ReflectionTestUtils.setField(dispatcher, "recordEventHandler", recordEventHandler);
        ReflectionTestUtils.setField(dispatcher, "objectMapper", new ObjectMapper().findAndRegisterModules());
        ReflectionTestUtils.setField(dispatcher, "recordEventConsumerState", recordEventConsumerState);
    }

    @Test
    void dispatchRecordCreatedShouldFallbackToLocalHandlerWhenKafkaDisabled() {
        kafkaFeatureProperties.setEnabled(false);

        dispatcher.dispatchRecordCreated(31L, buildRecord(11L));

        verify(recordEventHandler).handle(any());
        verify(kafkaProducerService, never()).sendMessage(any(), any(), any());
    }

    @Test
    void dispatchRecordUpdatedShouldPublishToKafkaWhenEnabled() {
        kafkaFeatureProperties.setEnabled(true);
        kafkaFeatureProperties.setListenerAutoStartup(true);
        kafkaFeatureProperties.setRecordEventTopic("moneykeeper-record-events");
        when(kafkaProducerService.isEnabled()).thenReturn(true);
        when(recordEventConsumerState.isReady()).thenReturn(true);

        dispatcher.dispatchRecordUpdated(31L, buildRecord(10L), buildRecord(11L));

        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaProducerService).sendMessage(eq("moneykeeper-record-events"), eq("31:11"), payloadCaptor.capture());
        assertTrue(payloadCaptor.getValue().contains("\"changeType\":\"UPDATED\""));
        verify(recordEventHandler, never()).handle(any());
    }

    @Test
    void dispatchRecordDeletedShouldFallbackToLocalHandlerWhenKafkaPublishFails() {
        kafkaFeatureProperties.setEnabled(true);
        kafkaFeatureProperties.setListenerAutoStartup(true);
        kafkaFeatureProperties.setRecordEventTopic("moneykeeper-record-events");
        when(kafkaProducerService.isEnabled()).thenReturn(true);
        when(recordEventConsumerState.isReady()).thenReturn(true);
        doThrow(new IllegalStateException("Kafka unavailable")).when(kafkaProducerService).sendMessage(any(), any(), any());

        dispatcher.dispatchRecordDeleted(31L, buildRecord(11L));

        verify(recordEventHandler).handle(any());
    }

    @Test
    void dispatchRecordCreatedShouldFallbackToLocalHandlerUntilConsumerIsReady() {
        kafkaFeatureProperties.setEnabled(true);
        kafkaFeatureProperties.setListenerAutoStartup(true);
        when(recordEventConsumerState.isReady()).thenReturn(false);

        dispatcher.dispatchRecordCreated(31L, buildRecord(11L));

        verify(recordEventHandler).handle(any());
        verify(kafkaProducerService, never()).sendMessage(any(), any(), any());
    }

    private MoneyKeeper buildRecord(Long id) {
        MoneyKeeper record = new MoneyKeeper();
        record.setId(id);
        record.setLedgerId(31L);
        record.setUserId(2L);
        record.setCategoryId(8L);
        record.setType("expense");
        record.setAmount(new BigDecimal("18.50"));
        record.setTransactionDate(LocalDate.of(2026, 3, 12));
        return record;
    }
}
