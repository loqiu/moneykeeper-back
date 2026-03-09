package com.loqiu.moneykeeper.service.impl;

import com.loqiu.moneykeeper.config.PaymentProperties;
import com.loqiu.moneykeeper.dto.MkCheckoutSession;
import com.loqiu.moneykeeper.dto.MkPaymentIntentDTO;
import com.loqiu.moneykeeper.response.MkApiResponse;
import com.loqiu.moneykeeper.service.PaymentStripeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PaymentStripeServiceImpl implements PaymentStripeService {

    @Autowired
    private PaymentProperties paymentProperties;

    @Override
    public MkApiResponse<MkPaymentIntentDTO> createPaymentIntent(Long amount, String currency) {
        if (amount == null || amount <= 0) {
            return MkApiResponse.error(400, "Amount must be greater than zero");
        }
        String resolvedCurrency = resolveCurrency(currency);
        if (resolvedCurrency == null) {
            return MkApiResponse.error(400, "Currency is required");
        }
        if (!paymentProperties.isEnabled()) {
            return MkApiResponse.error(503, "Payment module is disabled");
        }
        return MkApiResponse.error(501, paymentProperties.getProvider() + " payment integration is not implemented yet");
    }

    @Override
    public MkApiResponse<MkPaymentIntentDTO> confirmPaymentIntent(String paymentIntentId) {
        if (!StringUtils.hasText(paymentIntentId)) {
            return MkApiResponse.error(400, "Payment intent id is required");
        }
        if (!paymentProperties.isEnabled()) {
            return MkApiResponse.error(503, "Payment module is disabled");
        }
        return MkApiResponse.error(501, paymentProperties.getProvider() + " payment integration is not implemented yet");
    }

    @Override
    public MkApiResponse<MkPaymentIntentDTO> cancelPaymentIntent(String paymentIntentId) {
        if (!StringUtils.hasText(paymentIntentId)) {
            return MkApiResponse.error(400, "Payment intent id is required");
        }
        if (!paymentProperties.isEnabled()) {
            return MkApiResponse.error(503, "Payment module is disabled");
        }
        return MkApiResponse.error(501, paymentProperties.getProvider() + " payment integration is not implemented yet");
    }

    @Override
    public MkApiResponse<MkPaymentIntentDTO> createCheckoutSession(MkCheckoutSession checkoutSession) {
        if (checkoutSession == null) {
            return MkApiResponse.error(400, "Checkout session request is required");
        }
        if (!StringUtils.hasText(checkoutSession.getMode())) {
            return MkApiResponse.error(400, "Checkout mode is required");
        }
        if (!StringUtils.hasText(checkoutSession.getSuccess_url())) {
            return MkApiResponse.error(400, "Success URL is required");
        }
        if (!StringUtils.hasText(checkoutSession.getCancel_url())) {
            return MkApiResponse.error(400, "Cancel URL is required");
        }
        if (checkoutSession.getLine_items() == null || checkoutSession.getLine_items().isEmpty()) {
            return MkApiResponse.error(400, "At least one line item is required");
        }
        if (!paymentProperties.isEnabled()) {
            return MkApiResponse.error(503, "Payment module is disabled");
        }
        return MkApiResponse.error(501, paymentProperties.getProvider() + " checkout integration is not implemented yet");
    }

    private String resolveCurrency(String currency) {
        if (StringUtils.hasText(currency)) {
            return currency.trim().toLowerCase();
        }
        if (StringUtils.hasText(paymentProperties.getDefaultCurrency())) {
            return paymentProperties.getDefaultCurrency().trim().toLowerCase();
        }
        return null;
    }
}