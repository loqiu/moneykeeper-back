package com.loqiu.moneykeeper.controller;

import com.loqiu.moneykeeper.enums.MessageType;
import com.loqiu.moneykeeper.exception.GlobalExceptionHandler;
import com.loqiu.moneykeeper.service.NotificationService;
import com.loqiu.moneykeeper.service.SseEmitterService;
import com.loqiu.moneykeeper.util.RequestAuthUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private SseEmitterService sseEmitterService;

    @Mock
    private NotificationService notificationService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        NotificationController controller = new NotificationController();
        ReflectionTestUtils.setField(controller, "sseEmitterService", sseEmitterService);
        ReflectionTestUtils.setField(controller, "notificationService", notificationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void sendMessageShouldAcceptLowercaseMessageType() throws Exception {
        mockMvc.perform(post("/api/notifications/send/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 1L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "user")
                        .content("""
                                {
                                  "title": " Budget ",
                                  "message": " Alert ",
                                  "type": "warning"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string("Message sent"));

        verify(notificationService).sendMessage(1L, "Budget", "Alert", MessageType.WARNING);
    }

    @Test
    void subscribeShouldRejectCrossUserAccess() throws Exception {
        mockMvc.perform(get("/api/notifications/subscribe/2")
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 1L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "user"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You do not have permission to access this notification resource"));
    }

    @Test
    void broadcastShouldRejectBlankTitle() throws Exception {
        mockMvc.perform(post("/api/notifications/broadcast")
                        .contentType(MediaType.APPLICATION_JSON)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 1L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "admin")
                        .content("""
                                {
                                  "title": "   ",
                                  "message": "Alert",
                                  "type": "warning"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Title is required"));
    }
}