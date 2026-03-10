package com.loqiu.moneykeeper.controller;

import com.loqiu.moneykeeper.config.PaymentProperties;
import com.loqiu.moneykeeper.dto.PaymentCheckoutSessionDTO;
import com.loqiu.moneykeeper.dto.PaymentPlanDTO;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
        paymentProperties.setEnabled(true);
        paymentProperties.setProvider("stripe");
        paymentProperties.setDefaultCurrency("usd");
        paymentProperties.setSecretKey("sk_test_123");
        paymentProperties.setWebhookSecret("whsec_test_123");
        paymentProperties.setBillingPortalReturnUrl("https://app.example.com/account/billing");

        ReflectionTestUtils.setField(controller, "paymentStripeService", paymentStripeService);
        ReflectionTestUtils.setField(controller, "paymentProperties", paymentProperties);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getPaymentStatusShouldReturnStripeReadinessFlags() throws Exception {
        when(paymentStripeService.isReady()).thenReturn(false);

        mockMvc.perform(get("/api/payments/status")
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.enabled").value(true))
                .andExpect(jsonPath("$.data.apiReady").value(true))
                .andExpect(jsonPath("$.data.webhookReady").value(true))
                .andExpect(jsonPath("$.data.implemented").value(true));
    }

    @Test
    void listPlansShouldDelegateToService() throws Exception {
        when(paymentStripeService.listPlans()).thenReturn(MkApiResponse.success(List.of(
                PaymentPlanDTO.builder().code("pro_monthly").name("Pro Monthly").amountMinor(990L).currency("gbp").billingInterval("month").build()
        )));

        mockMvc.perform(get("/api/payments/plans")
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].code").value("pro_monthly"))
                .andExpect(jsonPath("$.data[0].amountMinor").value(990));
    }

    @Test
    void createCheckoutSessionShouldDelegateToService() throws Exception {
        when(paymentStripeService.createCheckoutSession(any(), eq(1L))).thenReturn(MkApiResponse.success(
                PaymentCheckoutSessionDTO.builder()
                        .orderNo("PO-001")
                        .planCode("pro_monthly")
                        .checkoutSessionId("cs_test_123")
                        .checkoutUrl("https://checkout.stripe.com/pay/cs_test_123")
                        .status("checkout_created")
                        .build()
        ));

        mockMvc.perform(post("/api/payments/checkout-sessions")
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "planCode": "pro_monthly",
                                  "successUrl": "https://app.example.com/billing/success",
                                  "cancelUrl": "https://app.example.com/billing/cancel"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.orderNo").value("PO-001"))
                .andExpect(jsonPath("$.data.checkoutSessionId").value("cs_test_123"));

        verify(paymentStripeService).createCheckoutSession(any(), eq(1L));
    }

    @Test
    void handleStripeWebhookShouldRejectMissingSignature() throws Exception {
        mockMvc.perform(post("/api/payments/webhooks/stripe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Stripe-Signature header is required"));
    }
}