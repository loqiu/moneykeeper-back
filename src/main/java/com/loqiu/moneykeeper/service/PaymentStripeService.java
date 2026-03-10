package com.loqiu.moneykeeper.service;

import com.loqiu.moneykeeper.dto.MkPaymentIntentDTO;
import com.loqiu.moneykeeper.dto.PaymentBillingPortalSessionDTO;
import com.loqiu.moneykeeper.dto.PaymentCheckoutSessionDTO;
import com.loqiu.moneykeeper.dto.PaymentOrderDTO;
import com.loqiu.moneykeeper.dto.PaymentPlanDTO;
import com.loqiu.moneykeeper.dto.PaymentSubscriptionDTO;
import com.loqiu.moneykeeper.response.MkApiResponse;
import com.loqiu.moneykeeper.vo.PaymentBillingPortalSessionRequest;
import com.loqiu.moneykeeper.vo.PaymentCheckoutSessionRequest;

import com.stripe.exception.SignatureVerificationException;

import java.util.List;

public interface PaymentStripeService {
    boolean isEnabled();

    boolean isReady();

    MkApiResponse<MkPaymentIntentDTO> createPaymentIntent(Long amount, String currency);

    MkApiResponse<MkPaymentIntentDTO> confirmPaymentIntent(String paymentIntentId);

    MkApiResponse<MkPaymentIntentDTO> cancelPaymentIntent(String paymentIntentId);

    MkApiResponse<List<PaymentPlanDTO>> listPlans();

    MkApiResponse<List<PaymentOrderDTO>> listOrders(Long userId);

    MkApiResponse<PaymentSubscriptionDTO> getCurrentSubscription(Long userId);

    MkApiResponse<PaymentCheckoutSessionDTO> createCheckoutSession(PaymentCheckoutSessionRequest request, Long userId);

    MkApiResponse<PaymentSubscriptionDTO> cancelCurrentSubscription(Long userId);

    MkApiResponse<PaymentBillingPortalSessionDTO> createBillingPortalSession(PaymentBillingPortalSessionRequest request,
                                                                             Long userId);

    void handleStripeWebhook(String payload, String signature) throws SignatureVerificationException;
}