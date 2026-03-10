package com.loqiu.moneykeeper.service.impl;

import com.loqiu.moneykeeper.config.PaymentProperties;
import com.loqiu.moneykeeper.entity.MembershipPlan;
import com.loqiu.moneykeeper.entity.PaymentSubscription;
import com.loqiu.moneykeeper.mapper.MembershipPlanMapper;
import com.loqiu.moneykeeper.mapper.PaymentOrderMapper;
import com.loqiu.moneykeeper.mapper.PaymentSubscriptionMapper;
import com.loqiu.moneykeeper.mapper.PaymentWebhookEventMapper;
import com.loqiu.moneykeeper.service.UserService;
import com.loqiu.moneykeeper.vo.PaymentCheckoutSessionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentStripeServiceImplTest {

    @Mock
    private MembershipPlanMapper membershipPlanMapper;

    @Mock
    private PaymentOrderMapper paymentOrderMapper;

    @Mock
    private PaymentSubscriptionMapper paymentSubscriptionMapper;

    @Mock
    private PaymentWebhookEventMapper paymentWebhookEventMapper;

    @Mock
    private UserService userService;

    private PaymentProperties paymentProperties;
    private PaymentStripeServiceImpl service;

    @BeforeEach
    void setUp() {
        paymentProperties = new PaymentProperties();
        paymentProperties.setEnabled(false);
        paymentProperties.setProvider("stripe");
        paymentProperties.setDefaultCurrency("usd");

        service = new PaymentStripeServiceImpl();
        ReflectionTestUtils.setField(service, "paymentProperties", paymentProperties);
        ReflectionTestUtils.setField(service, "membershipPlanMapper", membershipPlanMapper);
        ReflectionTestUtils.setField(service, "paymentOrderMapper", paymentOrderMapper);
        ReflectionTestUtils.setField(service, "paymentSubscriptionMapper", paymentSubscriptionMapper);
        ReflectionTestUtils.setField(service, "paymentWebhookEventMapper", paymentWebhookEventMapper);
        ReflectionTestUtils.setField(service, "userService", userService);
    }

    @Test
    void listPlansShouldReturnOnlyMappedPlans() {
        when(membershipPlanMapper.selectList(any())).thenReturn(List.of(
                MembershipPlan.builder()
                        .id(1L)
                        .code("pro_monthly")
                        .name("Pro Monthly")
                        .description("Monthly plan")
                        .currency("gbp")
                        .amountMinor(990L)
                        .billingInterval("month")
                        .active(1)
                        .build()
        ));

        var response = service.listPlans();

        assertEquals(200, response.getCode());
        assertEquals(1, response.getData().size());
        assertEquals("pro_monthly", response.getData().get(0).getCode());
    }

    @Test
    void getCurrentSubscriptionShouldReturnNoneWhenAbsent() {
        when(paymentSubscriptionMapper.selectOne(any())).thenReturn(null);

        var response = service.getCurrentSubscription(1L);

        assertEquals(200, response.getCode());
        assertEquals("none", response.getData().getStatus());
        assertFalse(response.getData().isActive());
    }

    @Test
    void createCheckoutSessionShouldRejectWhenPaymentIsNotReady() {
        PaymentCheckoutSessionRequest request = new PaymentCheckoutSessionRequest();
        request.setPlanCode("pro_monthly");
        request.setSuccessUrl("https://app.example.com/billing/success");
        request.setCancelUrl("https://app.example.com/billing/cancel");

        var response = service.createCheckoutSession(request, 1L);

        assertEquals(503, response.getCode());
        assertEquals("Payment module is disabled", response.getMessage());
    }

    @Test
    void getCurrentSubscriptionShouldMapStoredRecord() {
        when(paymentSubscriptionMapper.selectOne(any())).thenReturn(PaymentSubscription.builder()
                .id(1L)
                .userId(1L)
                .planId(2L)
                .planCode("pro_monthly")
                .planName("Pro Monthly")
                .currency("gbp")
                .amountMinor(990L)
                .billingInterval("month")
                .status("active")
                .currentPeriodStart(LocalDateTime.of(2026, 3, 1, 0, 0))
                .currentPeriodEnd(LocalDateTime.of(2026, 4, 1, 0, 0))
                .cancelAtPeriodEnd(0)
                .stripeSubscriptionId("sub_123")
                .build());

        var response = service.getCurrentSubscription(1L);

        assertEquals(200, response.getCode());
        assertEquals("active", response.getData().getStatus());
        assertEquals("pro_monthly", response.getData().getPlanCode());
        assertEquals("sub_123", response.getData().getStripeSubscriptionId());
    }
}