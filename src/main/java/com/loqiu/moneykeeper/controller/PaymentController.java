package com.loqiu.moneykeeper.controller;

import com.loqiu.moneykeeper.config.PaymentProperties;
import com.loqiu.moneykeeper.dto.MkCheckoutSession;
import com.loqiu.moneykeeper.dto.MkPaymentIntentDTO;
import com.loqiu.moneykeeper.response.MkApiResponse;
import com.loqiu.moneykeeper.service.PaymentStripeService;
import com.loqiu.moneykeeper.util.RequestAuthUtil;
import com.loqiu.moneykeeper.vo.PaymentIntentRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentStripeService paymentStripeService;

    @Autowired
    private PaymentProperties paymentProperties;

    @GetMapping("/status")
    public MkApiResponse<Map<String, Object>> getPaymentStatus(HttpServletRequest request) {
        RequestAuthUtil.requireCurrentUserId(request);
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("enabled", paymentProperties.isEnabled());
        status.put("provider", paymentProperties.getProvider());
        status.put("defaultCurrency", paymentProperties.getDefaultCurrency());
        status.put("implemented", false);
        return MkApiResponse.success(status);
    }

    @PostMapping("/intents")
    public MkApiResponse<MkPaymentIntentDTO> createPaymentIntent(@RequestBody PaymentIntentRequest request,
                                                                 HttpServletRequest httpRequest) {
        RequestAuthUtil.requireCurrentUserId(httpRequest);
        if (request == null) {
            return MkApiResponse.error(400, "Payment intent request is required");
        }
        return paymentStripeService.createPaymentIntent(request.getAmount(), request.getCurrency());
    }

    @PostMapping("/intents/{paymentIntentId}/confirm")
    public MkApiResponse<MkPaymentIntentDTO> confirmPaymentIntent(@PathVariable String paymentIntentId,
                                                                  HttpServletRequest request) {
        RequestAuthUtil.requireCurrentUserId(request);
        return paymentStripeService.confirmPaymentIntent(paymentIntentId);
    }

    @PostMapping("/intents/{paymentIntentId}/cancel")
    public MkApiResponse<MkPaymentIntentDTO> cancelPaymentIntent(@PathVariable String paymentIntentId,
                                                                 HttpServletRequest request) {
        RequestAuthUtil.requireCurrentUserId(request);
        return paymentStripeService.cancelPaymentIntent(paymentIntentId);
    }

    @PostMapping("/checkout-sessions")
    public MkApiResponse<MkPaymentIntentDTO> createCheckoutSession(@RequestBody MkCheckoutSession checkoutSession,
                                                                   HttpServletRequest request) {
        RequestAuthUtil.requireCurrentUserId(request);
        return paymentStripeService.createCheckoutSession(checkoutSession);
    }
}