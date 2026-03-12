package com.loqiu.moneykeeper.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loqiu.moneykeeper.config.KafkaFeatureProperties;
import com.loqiu.moneykeeper.service.ExportJobConsumerState;
import com.loqiu.moneykeeper.service.KafkaProducerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExportJobEventDispatcherImplTest {

    @Mock
    private KafkaProducerService kafkaProducerService;

    @Mock
    private ExportJobConsumerState exportJobConsumerState;

    private ExportJobEventDispatcherImpl dispatcher;
    private KafkaFeatureProperties kafkaFeatureProperties;

    @BeforeEach
    void setUp() {
        dispatcher = new ExportJobEventDispatcherImpl();
        kafkaFeatureProperties = new KafkaFeatureProperties();
        ReflectionTestUtils.setField(dispatcher, "kafkaProducerService", kafkaProducerService);
        ReflectionTestUtils.setField(dispatcher, "kafkaFeatureProperties", kafkaFeatureProperties);
        ReflectionTestUtils.setField(dispatcher, "exportJobConsumerState", exportJobConsumerState);
        ReflectionTestUtils.setField(dispatcher, "objectMapper", new ObjectMapper().findAndRegisterModules());
    }

    @Test
    void dispatchCreatedShouldPublishToKafkaWhenReady() {
        kafkaFeatureProperties.setEnabled(true);
        kafkaFeatureProperties.setListenerAutoStartup(true);
        kafkaFeatureProperties.setExportJobTopic("moneykeeper-export-job-events");
        when(kafkaProducerService.isEnabled()).thenReturn(true);
        when(exportJobConsumerState.isReady()).thenReturn(true);

        dispatcher.dispatchCreated(9L, 31L, 2L);

        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaProducerService).sendMessage(eq("moneykeeper-export-job-events"), eq("31:9"), payloadCaptor.capture());
        assertTrue(payloadCaptor.getValue().contains("\"eventType\":\"CREATED\""));
        assertTrue(payloadCaptor.getValue().contains("\"jobId\":9"));
    }

    @Test
    void dispatchCreatedShouldSkipKafkaUntilConsumerIsReady() {
        kafkaFeatureProperties.setEnabled(true);
        kafkaFeatureProperties.setListenerAutoStartup(true);
        when(exportJobConsumerState.isReady()).thenReturn(false);

        dispatcher.dispatchCreated(9L, 31L, 2L);

        verify(kafkaProducerService, never()).sendMessage(any(), any(), any());
    }
}
