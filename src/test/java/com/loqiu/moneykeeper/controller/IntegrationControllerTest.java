package com.loqiu.moneykeeper.controller;

import com.loqiu.moneykeeper.dto.IntegrationModuleStatusDTO;
import com.loqiu.moneykeeper.exception.GlobalExceptionHandler;
import com.loqiu.moneykeeper.service.IntegrationStatusService;
import com.loqiu.moneykeeper.util.RequestAuthUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class IntegrationControllerTest {

    @Mock
    private IntegrationStatusService integrationStatusService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        IntegrationController controller = new IntegrationController();
        ReflectionTestUtils.setField(controller, "integrationStatusService", integrationStatusService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getIntegrationStatusesShouldRejectNonAdminRequests() throws Exception {
        mockMvc.perform(get("/api/integrations/status")
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 1L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "user"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Admin role is required"));
    }

    @Test
    void getIntegrationStatusesShouldReturnStatusesForAdmin() throws Exception {
        when(integrationStatusService.getAllStatuses()).thenReturn(List.of(
                IntegrationModuleStatusDTO.builder()
                        .module("kafka")
                        .enabled(true)
                        .ready(true)
                        .implemented(true)
                        .summary("Kafka producer and consumer scaffolding is enabled")
                        .metadata(Map.of("producerReady", true))
                        .build()
        ));

        mockMvc.perform(get("/api/integrations/status")
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 1L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].module").value("kafka"))
                .andExpect(jsonPath("$[0].enabled").value(true));
    }
}