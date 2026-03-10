package com.loqiu.moneykeeper.controller;

import com.loqiu.moneykeeper.dto.KafkaMessageRecord;
import com.loqiu.moneykeeper.service.KafkaConsumerService;
import com.loqiu.moneykeeper.service.KafkaProducerService;
import com.loqiu.moneykeeper.util.RequestAuthUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class KafkaControllerTest {

    @Mock
    private KafkaProducerService kafkaProducerService;

    @Mock
    private KafkaConsumerService kafkaConsumerService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        KafkaController controller = new KafkaController(kafkaProducerService, kafkaConsumerService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getKafkaStatusShouldReturnStatusForAdmin() throws Exception {
        when(kafkaProducerService.isEnabled()).thenReturn(false);
        when(kafkaConsumerService.isEnabled()).thenReturn(false);
        when(kafkaConsumerService.getConsumedCount()).thenReturn(0L);

        mockMvc.perform(get("/api/kafka/status")
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 1L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.enabled").value(false))
                .andExpect(jsonPath("$.data.consumerEnabled").value(false));
    }

    @Test
    void sendShouldReturnDisabledResponseWhenKafkaModuleIsOff() throws Exception {
        doThrow(new IllegalStateException("Kafka module is disabled"))
                .when(kafkaProducerService).sendMessage(eq(null), eq("message"), eq("hello"));

        mockMvc.perform(post("/api/kafka/send")
                        .param("message", "hello")
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 1L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(503))
                .andExpect(jsonPath("$.message").value("Kafka module is disabled"));
    }

    @Test
    void getRecentMessagesShouldValidateLimit() throws Exception {
        mockMvc.perform(get("/api/kafka/messages")
                        .param("limit", "101")
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 1L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Limit must be between 1 and 100"));
    }

    @Test
    void getRecentMessagesShouldReturnRecords() throws Exception {
        when(kafkaConsumerService.getRecentMessages(1)).thenReturn(List.of(
                KafkaMessageRecord.builder()
                        .topic("quickstart-events")
                        .key("message")
                        .value("hello")
                        .receivedAt(LocalDateTime.of(2026, 3, 8, 12, 0))
                        .build()
        ));

        mockMvc.perform(get("/api/kafka/messages")
                        .param("limit", "1")
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 1L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].topic").value("quickstart-events"))
                .andExpect(jsonPath("$.data[0].value").value("hello"));
    }
}