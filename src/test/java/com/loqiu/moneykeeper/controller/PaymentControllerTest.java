package com.loqiu.moneykeeper.controller;

import com.loqiu.moneykeeper.config.PaymentProperties;
import com.loqiu.moneykeeper.response.MkApiResponse;
import com.loqiu.moneykeeper.service.PaymentStripeService;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentStripeService paymentStripeService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        PaymentController controller = new PaymentController();
        PaymentProperties paymentProperties = new PaymentProperties();
        paymentProperties.setEnabled(false);
        paymentProperties.setProvider("stripe");
        paymentProperties.setDefaultCurrency("usd");

        ReflectionTestUtils.setField(controller, "paymentStripeService", paymentStripeService);
        ReflectionTestUtils.setField(controller, "paymentProperties", paymentProperties);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getPaymentStatusShouldReturnConfiguredFlags() throws Exception {
        mockMvc.perform(get("/api/payments/status")
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.enabled").value(false))
                .andExpect(jsonPath("$.data.provider").value("stripe"));
    }

    @Test
    void createPaymentIntentShouldPassValidatedRequestToService() throws Exception {
        when(paymentStripeService.createPaymentIntent(eq(1200L), eq("usd")))
                .thenReturn(MkApiResponse.error(503, "Payment module is disabled"));

        mockMvc.perform(post("/api/payments/intents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 1L)
                        .content("""
                                {
                                  "amount": 1200,
                                  "currency": "usd"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(503))
                .andExpect(jsonPath("$.message").value("Payment module is disabled"));
    }
}