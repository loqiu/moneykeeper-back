package com.loqiu.moneykeeper.health;

import com.loqiu.moneykeeper.dto.IntegrationModuleStatusDTO;
import com.loqiu.moneykeeper.service.IntegrationStatusService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IntegrationHealthIndicatorTest {

    @Mock
    private IntegrationStatusService integrationStatusService;

    private IntegrationHealthIndicator integrationHealthIndicator;

    @BeforeEach
    void setUp() {
        integrationHealthIndicator = new IntegrationHealthIndicator();
        ReflectionTestUtils.setField(integrationHealthIndicator, "integrationStatusService", integrationStatusService);
    }

    @Test
    void healthShouldBeUpWhenAllEnabledModulesAreReady() {
        when(integrationStatusService.getAllStatuses()).thenReturn(List.of(
                IntegrationModuleStatusDTO.builder()
                        .module("kafka")
                        .enabled(true)
                        .ready(true)
                        .implemented(true)
                        .summary("Kafka is ready")
                        .metadata(Map.of("producerReady", true))
                        .build(),
                IntegrationModuleStatusDTO.builder()
                        .module("payment")
                        .enabled(false)
                        .ready(false)
                        .implemented(true)
                        .summary("Payment is disabled")
                        .build()
        ));

        Health health = integrationHealthIndicator.health();

        assertEquals(Status.UP, health.getStatus());
        assertEquals(1L, health.getDetails().get("enabledCount"));
        assertEquals(1L, health.getDetails().get("readyCount"));
    }

    @Test
    void healthShouldBeDownWhenAnEnabledModuleIsNotReady() {
        when(integrationStatusService.getAllStatuses()).thenReturn(List.of(
                IntegrationModuleStatusDTO.builder()
                        .module("elasticsearch")
                        .enabled(true)
                        .ready(false)
                        .implemented(true)
                        .summary("Elasticsearch client is not ready")
                        .build()
        ));

        Health health = integrationHealthIndicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertEquals(1L, health.getDetails().get("enabledCount"));
        assertEquals(0L, health.getDetails().get("readyCount"));
    }
}
