package com.loqiu.moneykeeper.controller;

import com.loqiu.moneykeeper.config.PaymentProperties;
import com.loqiu.moneykeeper.dto.MkPaymentIntentDTO;
import com.loqiu.moneykeeper.dto.PaymentBillingPortalSessionDTO;
import com.loqiu.moneykeeper.dto.PaymentCheckoutSessionDTO;
import com.loqiu.moneykeeper.dto.PaymentOrderDTO;
import com.loqiu.moneykeeper.dto.PaymentPlanDTO;
import com.loqiu.moneykeeper.dto.PaymentSubscriptionDTO;
import com.loqiu.moneykeeper.response.MkApiResponse;
import com.loqiu.moneykeeper.service.PaymentStripeService;
import com.loqiu.moneykeeper.util.RequestAuthUtil;
import com.loqiu.moneykeeper.vo.PaymentBillingPortalSessionRequest;
import com.loqiu.moneykeeper.vo.PaymentCheckoutSessionRequest;
import com.loqiu.moneykeeper.vo.PaymentIntentRequest;
import com.stripe.exception.SignatureVerificationException;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger logger = LogManager.getLogger(PaymentController.class);

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
        status.put("implemented", true);
        status.put("apiReady", paymentProperties.hasSecretKey());
        status.put("webhookReady", paymentProperties.hasWebhookSecret());
        status.put("ready", paymentStripeService.isReady());
        status.put("billingPortalReturnUrlConfigured", paymentProperties.hasBillingPortalReturnUrl());
        return MkApiResponse.success(status);
    }

    @GetMapping("/plans")
    public MkApiResponse<List<PaymentPlanDTO>> listPlans(HttpServletRequest request) {
        RequestAuthUtil.requireCurrentUserId(request);
        return paymentStripeService.listPlans();
    }

    @GetMapping("/orders")
    public MkApiResponse<List<PaymentOrderDTO>> listOrders(HttpServletRequest request) {
        Long currentUserId = RequestAuthUtil.requireCurrentUserId(request);
        return paymentStripeService.listOrders(currentUserId);
    }

    @GetMapping("/subscriptions/current")
    public MkApiResponse<PaymentSubscriptionDTO> getCurrentSubscription(HttpServletRequest request) {
        Long currentUserId = RequestAuthUtil.requireCurrentUserId(request);
        return paymentStripeService.getCurrentSubscription(currentUserId);
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
    public MkApiResponse<PaymentCheckoutSessionDTO> createCheckoutSession(@RequestBody PaymentCheckoutSessionRequest checkoutSession,
                                                                          HttpServletRequest request) {
        Long currentUserId = RequestAuthUtil.requireCurrentUserId(request);
        return paymentStripeService.createCheckoutSession(checkoutSession, currentUserId);
    }

    @PostMapping("/subscriptions/current/cancel")
    public MkApiResponse<PaymentSubscriptionDTO> cancelCurrentSubscription(HttpServletRequest request) {
        Long currentUserId = RequestAuthUtil.requireCurrentUserId(request);
        return paymentStripeService.cancelCurrentSubscription(currentUserId);
    }

    @PostMapping("/billing-portal-sessions")
    public MkApiResponse<PaymentBillingPortalSessionDTO> createBillingPortalSession(
            @RequestBody(required = false) PaymentBillingPortalSessionRequest billingPortalRequest,
            HttpServletRequest request) {
        Long currentUserId = RequestAuthUtil.requireCurrentUserId(request);
        return paymentStripeService.createBillingPortalSession(billingPortalRequest, currentUserId);
    }

    @PostMapping(value = "/webhooks/stripe", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> handleStripeWebhook(@RequestBody(required = false) String payload,
                                                      @RequestHeader(name = "Stripe-Signature", required = false) String signature) {
        if (!paymentProperties.isEnabled()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Payment module is disabled");
        }
        if (!paymentProperties.hasWebhookSecret()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Stripe webhook secret is not configured");
        }
        if (!StringUtils.hasText(payload)) {
            return ResponseEntity.badRequest().body("Stripe payload is required");
        }
        if (!StringUtils.hasText(signature)) {
            return ResponseEntity.badRequest().body("Stripe-Signature header is required");
        }

        try {
            paymentStripeService.handleStripeWebhook(payload, signature);
            return ResponseEntity.ok("ok");
        } catch (SignatureVerificationException ex) {
            logger.warn("Rejected Stripe webhook due to invalid signature: {}", ex.getMessage());
            return ResponseEntity.badRequest().body("Invalid Stripe signature");
        } catch (IllegalArgumentException ex) {
            logger.warn("Rejected Stripe webhook payload: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            logger.error("Failed to process Stripe webhook", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Webhook processing failed");
        }
    }
}