package com.loqiu.moneykeeper.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.loqiu.moneykeeper.config.PaymentProperties;
import com.loqiu.moneykeeper.dto.MkPaymentIntentDTO;
import com.loqiu.moneykeeper.dto.PaymentBillingPortalSessionDTO;
import com.loqiu.moneykeeper.dto.PaymentCheckoutSessionDTO;
import com.loqiu.moneykeeper.dto.PaymentOrderDTO;
import com.loqiu.moneykeeper.dto.PaymentPlanDTO;
import com.loqiu.moneykeeper.dto.PaymentSubscriptionDTO;
import com.loqiu.moneykeeper.entity.MembershipPlan;
import com.loqiu.moneykeeper.entity.PaymentOrder;
import com.loqiu.moneykeeper.entity.PaymentSubscription;
import com.loqiu.moneykeeper.entity.PaymentWebhookEvent;
import com.loqiu.moneykeeper.entity.User;
import com.loqiu.moneykeeper.mapper.MembershipPlanMapper;
import com.loqiu.moneykeeper.mapper.PaymentOrderMapper;
import com.loqiu.moneykeeper.mapper.PaymentSubscriptionMapper;
import com.loqiu.moneykeeper.mapper.PaymentWebhookEventMapper;
import com.loqiu.moneykeeper.response.MkApiResponse;
import com.loqiu.moneykeeper.service.PaymentStripeService;
import com.loqiu.moneykeeper.service.UserService;
import com.loqiu.moneykeeper.vo.PaymentBillingPortalSessionRequest;
import com.loqiu.moneykeeper.vo.PaymentCheckoutSessionRequest;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.Invoice;
import com.stripe.model.StripeObject;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import com.stripe.param.SubscriptionUpdateParams;
import com.stripe.param.billingportal.SessionCreateParams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentStripeServiceImpl implements PaymentStripeService {

    private static final Logger logger = LogManager.getLogger(PaymentStripeServiceImpl.class);

    private static final String ORDER_TYPE_SUBSCRIPTION_CHECKOUT = "subscription_checkout";
    private static final String ORDER_TYPE_SUBSCRIPTION_INVOICE = "subscription_invoice";
    private static final String ORDER_STATUS_PENDING = "pending";
    private static final String ORDER_STATUS_CHECKOUT_CREATED = "checkout_created";
    private static final String ORDER_STATUS_CHECKOUT_COMPLETED = "checkout_completed";
    private static final String ORDER_STATUS_PAID = "paid";
    private static final String ORDER_STATUS_PAYMENT_FAILED = "payment_failed";
    private static final Set<String> OPEN_SUBSCRIPTION_STATUSES = Set.of(
            "trialing", "active", "past_due", "incomplete", "unpaid"
    );
    private static final Set<String> ACTIVE_ENTITLEMENT_STATUSES = Set.of(
            "trialing", "active"
    );

    @Autowired
    private PaymentProperties paymentProperties;

    @Autowired
    private MembershipPlanMapper membershipPlanMapper;

    @Autowired
    private PaymentOrderMapper paymentOrderMapper;

    @Autowired
    private PaymentSubscriptionMapper paymentSubscriptionMapper;

    @Autowired
    private PaymentWebhookEventMapper paymentWebhookEventMapper;

    @Autowired
    private UserService userService;

    @Override
    public boolean isEnabled() {
        return paymentProperties.isEnabled();
    }

    @Override
    public boolean isReady() {
        return paymentProperties.isEnabled() && paymentProperties.hasSecretKey() && paymentProperties.hasWebhookSecret();
    }

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
        return MkApiResponse.error(501, "Stripe payment intent flow is not implemented; use hosted checkout sessions for subscriptions");
    }

    @Override
    public MkApiResponse<MkPaymentIntentDTO> confirmPaymentIntent(String paymentIntentId) {
        if (!StringUtils.hasText(paymentIntentId)) {
            return MkApiResponse.error(400, "Payment intent id is required");
        }
        if (!paymentProperties.isEnabled()) {
            return MkApiResponse.error(503, "Payment module is disabled");
        }
        return MkApiResponse.error(501, "Stripe payment intent confirmation is not implemented; subscription flows use webhook-confirmed checkout sessions");
    }

    @Override
    public MkApiResponse<MkPaymentIntentDTO> cancelPaymentIntent(String paymentIntentId) {
        if (!StringUtils.hasText(paymentIntentId)) {
            return MkApiResponse.error(400, "Payment intent id is required");
        }
        if (!paymentProperties.isEnabled()) {
            return MkApiResponse.error(503, "Payment module is disabled");
        }
        return MkApiResponse.error(501, "Stripe payment intent cancellation is not implemented; subscription flows use billing portal management");
    }

    @Override
    public MkApiResponse<List<PaymentPlanDTO>> listPlans() {
        List<PaymentPlanDTO> plans = membershipPlanMapper.selectList(new QueryWrapper<MembershipPlan>()
                        .eq("active", 1)
                        .orderByAsc("amount_minor", "id"))
                .stream()
                .map(this::toPlanDto)
                .collect(Collectors.toList());
        return MkApiResponse.success(plans);
    }

    @Override
    public MkApiResponse<List<PaymentOrderDTO>> listOrders(Long userId) {
        List<PaymentOrderDTO> orders = paymentOrderMapper.selectList(new QueryWrapper<PaymentOrder>()
                        .eq("user_id", userId)
                        .orderByDesc("created_at"))
                .stream()
                .map(this::toOrderDto)
                .collect(Collectors.toList());
        return MkApiResponse.success(orders);
    }

    @Override
    public MkApiResponse<PaymentSubscriptionDTO> getCurrentSubscription(Long userId) {
        PaymentSubscription subscription = findSubscriptionByUserId(userId);
        if (subscription == null) {
            return MkApiResponse.success(PaymentSubscriptionDTO.none());
        }
        return MkApiResponse.success(toSubscriptionDto(subscription));
    }

    @Override
    public MkApiResponse<PaymentCheckoutSessionDTO> createCheckoutSession(PaymentCheckoutSessionRequest request,
                                                                          Long userId) {
        if (request == null) {
            return MkApiResponse.error(400, "Checkout session request is required");
        }
        String planCode = trimToNull(request.getPlanCode());
        if (planCode == null) {
            return MkApiResponse.error(400, "Plan code is required");
        }
        String successUrlError = validateRedirectUrl(request.getSuccessUrl(), "Success URL");
        if (successUrlError != null) {
            return MkApiResponse.error(400, successUrlError);
        }
        String cancelUrlError = validateRedirectUrl(request.getCancelUrl(), "Cancel URL");
        if (cancelUrlError != null) {
            return MkApiResponse.error(400, cancelUrlError);
        }
        if (!isReady()) {
            return paymentNotReadyResponse();
        }

        MembershipPlan plan = findActivePlanByCode(planCode);
        if (plan == null) {
            return MkApiResponse.error(404, "Membership plan not found");
        }

        PaymentSubscription existingSubscription = findSubscriptionByUserId(userId);
        if (existingSubscription != null && isOpenSubscriptionStatus(existingSubscription.getStatus())) {
            return MkApiResponse.error(409, "User already has a managed subscription; use the billing portal instead");
        }

        User user = userService.getById(userId);
        if (user == null) {
            return MkApiResponse.error(404, "User not found");
        }

        PaymentOrder order = PaymentOrder.builder()
                .orderNo(generateOrderNo())
                .userId(userId)
                .planId(plan.getId())
                .planCode(plan.getCode())
                .planName(plan.getName())
                .orderType(ORDER_TYPE_SUBSCRIPTION_CHECKOUT)
                .status(ORDER_STATUS_PENDING)
                .currency(plan.getCurrency())
                .amountMinor(plan.getAmountMinor())
                .idempotencyKey(generateIdempotencyKey())
                .build();
        paymentOrderMapper.insert(order);

        try {
            Session session = createStripeCheckoutSession(request, user, plan, order, existingSubscription);
            order.setStatus(ORDER_STATUS_CHECKOUT_CREATED);
            order.setStripeCheckoutSessionId(session.getId());
            order.setStripePaymentIntentId(trimToNull(objectToString(session.getPaymentIntent())));
            order.setStripeSubscriptionId(trimToNull(objectToString(session.getSubscription())));
            order.setStripeCustomerId(trimToNull(objectToString(session.getCustomer())));
            paymentOrderMapper.updateById(order);

            PaymentCheckoutSessionDTO response = PaymentCheckoutSessionDTO.builder()
                    .orderNo(order.getOrderNo())
                    .planCode(plan.getCode())
                    .checkoutSessionId(session.getId())
                    .checkoutUrl(session.getUrl())
                    .status(order.getStatus())
                    .build();
            return MkApiResponse.success("Checkout session created", response);
        } catch (StripeException ex) {
            logger.error("Failed to create Stripe checkout session for user {} and plan {}", userId, planCode, ex);
            order.setStatus(ORDER_STATUS_PAYMENT_FAILED);
            order.setFailureMessage(abbreviate(ex.getMessage(), 255));
            paymentOrderMapper.updateById(order);
            return MkApiResponse.error(500, "Failed to create Stripe checkout session");
        }
    }

    @Override
    public MkApiResponse<PaymentSubscriptionDTO> cancelCurrentSubscription(Long userId) {
        if (!isApiConfigured()) {
            return paymentApiNotConfiguredResponse();
        }

        PaymentSubscription currentSubscription = findSubscriptionByUserId(userId);
        if (currentSubscription == null || !StringUtils.hasText(currentSubscription.getStripeSubscriptionId())) {
            return MkApiResponse.error(404, "Current subscription not found");
        }
        if (!isOpenSubscriptionStatus(currentSubscription.getStatus())) {
            return MkApiResponse.error(409, "Current subscription is already inactive");
        }

        try {
            Subscription stripeSubscription = Subscription.retrieve(currentSubscription.getStripeSubscriptionId(), requestOptions());
            Subscription updatedSubscription = stripeSubscription.update(
                    SubscriptionUpdateParams.builder().setCancelAtPeriodEnd(Boolean.TRUE).build(),
                    requestOptions()
            );
            MembershipPlan plan = findPlanById(currentSubscription.getPlanId());
            if (plan == null) {
                plan = MembershipPlan.builder()
                        .id(currentSubscription.getPlanId())
                        .code(currentSubscription.getPlanCode())
                        .name(currentSubscription.getPlanName())
                        .currency(currentSubscription.getCurrency())
                        .amountMinor(currentSubscription.getAmountMinor())
                        .billingInterval(currentSubscription.getBillingInterval())
                        .stripePriceId(currentSubscription.getStripePriceId())
                        .build();
            }
            PaymentSubscription savedSubscription = upsertSubscriptionFromStripe(
                    updatedSubscription,
                    userId,
                    plan,
                    currentSubscription.getLatestOrderId()
            );
            return MkApiResponse.success("Subscription will cancel at period end", toSubscriptionDto(savedSubscription));
        } catch (StripeException ex) {
            logger.error("Failed to cancel Stripe subscription for user {}", userId, ex);
            return MkApiResponse.error(500, "Failed to cancel current subscription");
        }
    }

    @Override
    public MkApiResponse<PaymentBillingPortalSessionDTO> createBillingPortalSession(PaymentBillingPortalSessionRequest request,
                                                                                    Long userId) {
        if (!isApiConfigured()) {
            return paymentApiNotConfiguredResponse();
        }

        PaymentSubscription currentSubscription = findSubscriptionByUserId(userId);
        if (currentSubscription == null || !StringUtils.hasText(currentSubscription.getStripeCustomerId())) {
            return MkApiResponse.error(404, "Stripe customer was not found for the current user");
        }

        String returnUrl = request == null ? null : trimToNull(request.getReturnUrl());
        if (returnUrl == null) {
            returnUrl = trimToNull(paymentProperties.getBillingPortalReturnUrl());
        }
        String returnUrlError = validateRedirectUrl(returnUrl, "Return URL");
        if (returnUrlError != null) {
            return MkApiResponse.error(400, returnUrlError);
        }

        try {
            SessionCreateParams params = SessionCreateParams.builder()
                    .setCustomer(currentSubscription.getStripeCustomerId())
                    .setReturnUrl(returnUrl)
                    .build();
            com.stripe.model.billingportal.Session portalSession = com.stripe.model.billingportal.Session.create(params, requestOptions());
            return MkApiResponse.success(PaymentBillingPortalSessionDTO.builder().url(portalSession.getUrl()).build());
        } catch (StripeException ex) {
            logger.error("Failed to create Stripe billing portal session for user {}", userId, ex);
            return MkApiResponse.error(500, "Failed to create Stripe billing portal session");
        }
    }

    @Override
    public void handleStripeWebhook(String payload, String signature) throws SignatureVerificationException {
        if (!paymentProperties.isEnabled()) {
            throw new IllegalStateException("Payment module is disabled");
        }
        if (!paymentProperties.hasWebhookSecret()) {
            throw new IllegalStateException("Stripe webhook secret is not configured");
        }
        if (!StringUtils.hasText(payload)) {
            throw new IllegalArgumentException("Stripe payload is required");
        }
        if (!StringUtils.hasText(signature)) {
            throw new IllegalArgumentException("Stripe-Signature header is required");
        }

        Event event;
        try {
            event = Webhook.constructEvent(payload, signature, paymentProperties.getWebhookSecret());
        } catch (SignatureVerificationException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Invalid Stripe payload", ex);
        }

        PaymentWebhookEvent webhookEvent = findWebhookEventByStripeEventId(event.getId());
        if (webhookEvent != null && webhookEvent.getProcessed() != null && webhookEvent.getProcessed() == 1) {
            return;
        }
        if (webhookEvent == null) {
            webhookEvent = PaymentWebhookEvent.builder()
                    .stripeEventId(event.getId())
                    .eventType(event.getType())
                    .processed(0)
                    .payloadJson(payload)
                    .receivedAt(LocalDateTime.now())
                    .build();
            paymentWebhookEventMapper.insert(webhookEvent);
        }

        try {
            switch (event.getType()) {
                case "checkout.session.completed" -> handleCheckoutSessionCompleted(event, webhookEvent);
                case "customer.subscription.updated", "customer.subscription.deleted" -> handleSubscriptionLifecycleEvent(event, webhookEvent);
                case "invoice.paid" -> handleInvoiceEvent(event, webhookEvent, true);
                case "invoice.payment_failed" -> handleInvoiceEvent(event, webhookEvent, false);
                default -> logger.info("Ignoring Stripe webhook event type {}", event.getType());
            }
            webhookEvent.setProcessed(1);
            webhookEvent.setProcessedAt(LocalDateTime.now());
            webhookEvent.setErrorMessage(null);
            paymentWebhookEventMapper.updateById(webhookEvent);
        } catch (RuntimeException ex) {
            webhookEvent.setErrorMessage(abbreviate(ex.getMessage(), 255));
            paymentWebhookEventMapper.updateById(webhookEvent);
            throw ex;
        } catch (Exception ex) {
            webhookEvent.setErrorMessage(abbreviate(ex.getMessage(), 255));
            paymentWebhookEventMapper.updateById(webhookEvent);
            throw new IllegalStateException("Stripe webhook processing failed", ex);
        }
    }

    private Session createStripeCheckoutSession(PaymentCheckoutSessionRequest request,
                                                User user,
                                                MembershipPlan plan,
                                                PaymentOrder order,
                                                PaymentSubscription existingSubscription) throws StripeException {
        com.stripe.param.checkout.SessionCreateParams.Builder paramsBuilder = com.stripe.param.checkout.SessionCreateParams.builder()
                .setMode(com.stripe.param.checkout.SessionCreateParams.Mode.SUBSCRIPTION)
                .setSuccessUrl(request.getSuccessUrl().trim())
                .setCancelUrl(request.getCancelUrl().trim())
                .setClientReferenceId(String.valueOf(user.getId()))
                .putMetadata("userId", String.valueOf(user.getId()))
                .putMetadata("orderNo", order.getOrderNo())
                .putMetadata("planCode", plan.getCode())
                .addLineItem(com.stripe.param.checkout.SessionCreateParams.LineItem.builder()
                        .setPrice(plan.getStripePriceId())
                        .setQuantity(1L)
                        .build())
                .setSubscriptionData(com.stripe.param.checkout.SessionCreateParams.SubscriptionData.builder()
                        .putMetadata("userId", String.valueOf(user.getId()))
                        .putMetadata("orderNo", order.getOrderNo())
                        .putMetadata("planCode", plan.getCode())
                        .build());

        if (existingSubscription != null && StringUtils.hasText(existingSubscription.getStripeCustomerId())) {
            paramsBuilder.setCustomer(existingSubscription.getStripeCustomerId());
        } else if (StringUtils.hasText(user.getEmail())) {
            paramsBuilder.setCustomerEmail(user.getEmail());
        }

        return Session.create(paramsBuilder.build(), requestOptions(order.getIdempotencyKey()));
    }

    private void handleCheckoutSessionCompleted(Event event, PaymentWebhookEvent webhookEvent) throws StripeException {
        Session session = deserialize(event, Session.class);
        if (session == null) {
            logger.warn("Stripe checkout.session.completed webhook did not contain a Session object");
            return;
        }

        PaymentOrder order = findOrderByStripeCheckoutSessionId(session.getId());
        if (order == null) {
            String orderNo = session.getMetadata() == null ? null : trimToNull(session.getMetadata().get("orderNo"));
            if (orderNo != null) {
                order = findOrderByOrderNo(orderNo);
            }
        }

        Long userId = order != null ? order.getUserId() : parseLong(session.getMetadata() == null ? null : session.getMetadata().get("userId"));
        MembershipPlan plan = order != null
                ? findPlanById(order.getPlanId())
                : findPlanByCode(session.getMetadata() == null ? null : session.getMetadata().get("planCode"));

        if (order != null) {
            order.setStripeCheckoutSessionId(session.getId());
            order.setStripeCustomerId(trimToNull(objectToString(session.getCustomer())));
            order.setStripeSubscriptionId(trimToNull(objectToString(session.getSubscription())));
            order.setStripePaymentIntentId(trimToNull(objectToString(session.getPaymentIntent())));
            if ("paid".equalsIgnoreCase(session.getPaymentStatus())) {
                order.setStatus(ORDER_STATUS_PAID);
                order.setPaidAt(LocalDateTime.now());
            } else {
                order.setStatus(ORDER_STATUS_CHECKOUT_COMPLETED);
            }
            paymentOrderMapper.updateById(order);
            webhookEvent.setRelatedOrderId(order.getId());
        }

        String stripeSubscriptionId = trimToNull(objectToString(session.getSubscription()));
        if (stripeSubscriptionId != null && userId != null && plan != null) {
            Subscription stripeSubscription = Subscription.retrieve(stripeSubscriptionId, requestOptions());
            PaymentSubscription subscription = upsertSubscriptionFromStripe(stripeSubscription, userId, plan, order == null ? null : order.getId());
            webhookEvent.setRelatedSubscriptionId(subscription.getId());
        }
    }

    private void handleSubscriptionLifecycleEvent(Event event, PaymentWebhookEvent webhookEvent) {
        Subscription stripeSubscription = deserialize(event, Subscription.class);
        if (stripeSubscription == null) {
            logger.warn("Stripe subscription webhook {} did not contain a Subscription object", event.getType());
            return;
        }

        PaymentSubscription existingSubscription = findSubscriptionByStripeSubscriptionId(stripeSubscription.getId());
        Long userId = existingSubscription != null
                ? existingSubscription.getUserId()
                : parseLong(stripeSubscription.getMetadata() == null ? null : stripeSubscription.getMetadata().get("userId"));
        MembershipPlan plan = existingSubscription != null
                ? findPlanById(existingSubscription.getPlanId())
                : resolvePlanFromStripeSubscription(stripeSubscription,
                stripeSubscription.getMetadata() == null ? null : stripeSubscription.getMetadata().get("planCode"));
        Long latestOrderId = existingSubscription == null ? null : existingSubscription.getLatestOrderId();

        if (userId == null || plan == null) {
            logger.warn("Unable to resolve local subscription owner for Stripe subscription {}", stripeSubscription.getId());
            return;
        }

        PaymentSubscription savedSubscription = upsertSubscriptionFromStripe(stripeSubscription, userId, plan, latestOrderId);
        webhookEvent.setRelatedSubscriptionId(savedSubscription.getId());
        if (savedSubscription.getLatestOrderId() != null) {
            webhookEvent.setRelatedOrderId(savedSubscription.getLatestOrderId());
        }
    }

    private void handleInvoiceEvent(Event event, PaymentWebhookEvent webhookEvent, boolean paid) {
        Invoice invoice = deserialize(event, Invoice.class);
        if (invoice == null) {
            logger.warn("Stripe invoice webhook {} did not contain an Invoice object", event.getType());
            return;
        }

        String stripeSubscriptionId = resolveStripeSubscriptionId(invoice);
        if (stripeSubscriptionId == null) {
            return;
        }

        PaymentSubscription subscription = findSubscriptionByStripeSubscriptionId(stripeSubscriptionId);
        if (subscription == null) {
            logger.warn("Unable to resolve local subscription for Stripe invoice {}", invoice.getId());
            return;
        }

        PaymentOrder order = findOrderByStripeInvoiceId(invoice.getId());
        if (order == null) {
            order = findLatestCheckoutOrderWithoutInvoice(stripeSubscriptionId);
        }
        if (order == null) {
            order = PaymentOrder.builder()
                    .orderNo(generateOrderNo())
                    .userId(subscription.getUserId())
                    .planId(subscription.getPlanId())
                    .planCode(subscription.getPlanCode())
                    .planName(subscription.getPlanName())
                    .orderType(ORDER_TYPE_SUBSCRIPTION_INVOICE)
                    .status(paid ? ORDER_STATUS_PAID : ORDER_STATUS_PAYMENT_FAILED)
                    .currency(trimToNull(invoice.getCurrency()) == null ? subscription.getCurrency() : invoice.getCurrency())
                    .amountMinor(paid ? nullSafeLong(invoice.getAmountPaid()) : nullSafeLong(invoice.getAmountDue()))
                    .stripeInvoiceId(invoice.getId())
                    .stripeSubscriptionId(stripeSubscriptionId)
                    .stripeCustomerId(trimToNull(objectToString(invoice.getCustomer())))
                    .stripePaymentIntentId(resolveInvoicePaymentIntentId(invoice))
                    .idempotencyKey("invoice-" + invoice.getId())
                    .paidAt(paid ? LocalDateTime.now() : null)
                    .failureMessage(paid ? null : "Stripe invoice payment failed")
                    .build();
            paymentOrderMapper.insert(order);
        } else {
            order.setStatus(paid ? ORDER_STATUS_PAID : ORDER_STATUS_PAYMENT_FAILED);
            order.setCurrency(trimToNull(invoice.getCurrency()) == null ? order.getCurrency() : invoice.getCurrency());
            order.setAmountMinor(paid ? nullSafeLong(invoice.getAmountPaid()) : nullSafeLong(invoice.getAmountDue()));
            order.setStripeInvoiceId(invoice.getId());
            order.setStripeSubscriptionId(stripeSubscriptionId);
            order.setStripeCustomerId(trimToNull(objectToString(invoice.getCustomer())));
            order.setStripePaymentIntentId(resolveInvoicePaymentIntentId(invoice));
            order.setPaidAt(paid ? LocalDateTime.now() : null);
            order.setFailureMessage(paid ? null : "Stripe invoice payment failed");
            paymentOrderMapper.updateById(order);
        }

        subscription.setLatestOrderId(order.getId());
        paymentSubscriptionMapper.updateById(subscription);
        webhookEvent.setRelatedOrderId(order.getId());
        webhookEvent.setRelatedSubscriptionId(subscription.getId());
    }

    private <T extends StripeObject> T deserialize(Event event, Class<T> type) {
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        if (dataObjectDeserializer == null) {
            return null;
        }
        return dataObjectDeserializer.getObject()
                .filter(type::isInstance)
                .map(type::cast)
                .orElse(null);
    }

    private PaymentSubscription upsertSubscriptionFromStripe(Subscription stripeSubscription,
                                                             Long userId,
                                                             MembershipPlan plan,
                                                             Long latestOrderId) {
        PaymentSubscription existingSubscription = findSubscriptionByStripeSubscriptionId(stripeSubscription.getId());
        if (existingSubscription == null) {
            existingSubscription = findSubscriptionByUserId(userId);
        }

        PaymentSubscription subscription = existingSubscription == null ? new PaymentSubscription() : existingSubscription;
        subscription.setUserId(userId);
        subscription.setPlanId(plan.getId());
        subscription.setPlanCode(plan.getCode());
        subscription.setPlanName(plan.getName());
        subscription.setCurrency(plan.getCurrency());
        subscription.setAmountMinor(plan.getAmountMinor());
        subscription.setBillingInterval(plan.getBillingInterval());
        subscription.setStatus(trimToNull(stripeSubscription.getStatus()));
        subscription.setStripeCustomerId(trimToNull(objectToString(stripeSubscription.getCustomer())));
        subscription.setStripeSubscriptionId(stripeSubscription.getId());
        subscription.setStripePriceId(resolveStripePriceId(stripeSubscription, plan));
        subscription.setCurrentPeriodStart(resolveSubscriptionCurrentPeriodStart(stripeSubscription));
        subscription.setCurrentPeriodEnd(resolveSubscriptionCurrentPeriodEnd(stripeSubscription));
        subscription.setCancelAtPeriodEnd(Boolean.TRUE.equals(stripeSubscription.getCancelAtPeriodEnd()) ? 1 : 0);
        subscription.setCanceledAt(toLocalDateTime(stripeSubscription.getCanceledAt()));
        if (latestOrderId != null) {
            subscription.setLatestOrderId(latestOrderId);
        }

        if (subscription.getId() == null) {
            paymentSubscriptionMapper.insert(subscription);
        } else {
            paymentSubscriptionMapper.updateById(subscription);
        }
        return subscription;
    }

    private MembershipPlan resolvePlanFromStripeSubscription(Subscription stripeSubscription, String metadataPlanCode) {
        MembershipPlan plan = findPlanByCode(metadataPlanCode);
        if (plan != null) {
            return plan;
        }
        return findPlanByStripePriceId(resolveStripePriceId(stripeSubscription, null));
    }

    private String resolveStripePriceId(Subscription stripeSubscription, MembershipPlan fallbackPlan) {
        if (stripeSubscription.getItems() != null
                && stripeSubscription.getItems().getData() != null
                && !stripeSubscription.getItems().getData().isEmpty()
                && stripeSubscription.getItems().getData().get(0).getPrice() != null) {
            return trimToNull(stripeSubscription.getItems().getData().get(0).getPrice().getId());
        }
        return fallbackPlan == null ? null : fallbackPlan.getStripePriceId();
    }

    private PaymentPlanDTO toPlanDto(MembershipPlan plan) {
        return PaymentPlanDTO.builder()
                .code(plan.getCode())
                .name(plan.getName())
                .description(plan.getDescription())
                .currency(plan.getCurrency())
                .amountMinor(plan.getAmountMinor())
                .billingInterval(plan.getBillingInterval())
                .build();
    }

    private PaymentOrderDTO toOrderDto(PaymentOrder order) {
        return PaymentOrderDTO.builder()
                .orderNo(order.getOrderNo())
                .planCode(order.getPlanCode())
                .planName(order.getPlanName())
                .orderType(order.getOrderType())
                .status(order.getStatus())
                .currency(order.getCurrency())
                .amountMinor(order.getAmountMinor())
                .stripeCheckoutSessionId(order.getStripeCheckoutSessionId())
                .stripeInvoiceId(order.getStripeInvoiceId())
                .stripeSubscriptionId(order.getStripeSubscriptionId())
                .paidAt(order.getPaidAt())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private PaymentSubscriptionDTO toSubscriptionDto(PaymentSubscription subscription) {
        return PaymentSubscriptionDTO.builder()
                .active(isActiveEntitlementStatus(subscription.getStatus()))
                .status(defaultText(subscription.getStatus(), "none"))
                .planCode(subscription.getPlanCode())
                .planName(subscription.getPlanName())
                .currency(subscription.getCurrency())
                .amountMinor(subscription.getAmountMinor())
                .billingInterval(subscription.getBillingInterval())
                .currentPeriodStart(subscription.getCurrentPeriodStart())
                .currentPeriodEnd(subscription.getCurrentPeriodEnd())
                .cancelAtPeriodEnd(subscription.getCancelAtPeriodEnd() != null && subscription.getCancelAtPeriodEnd() == 1)
                .canceledAt(subscription.getCanceledAt())
                .stripeSubscriptionId(subscription.getStripeSubscriptionId())
                .build();
    }

    private MembershipPlan findActivePlanByCode(String planCode) {
        return membershipPlanMapper.selectOne(new QueryWrapper<MembershipPlan>()
                .eq("code", trimToNull(planCode))
                .eq("active", 1)
                .last("limit 1"));
    }

    private MembershipPlan findPlanByCode(String planCode) {
        String normalizedPlanCode = trimToNull(planCode);
        if (normalizedPlanCode == null) {
            return null;
        }
        return membershipPlanMapper.selectOne(new QueryWrapper<MembershipPlan>()
                .eq("code", normalizedPlanCode)
                .last("limit 1"));
    }

    private MembershipPlan findPlanById(Long planId) {
        return planId == null ? null : membershipPlanMapper.selectById(planId);
    }

    private MembershipPlan findPlanByStripePriceId(String stripePriceId) {
        String normalizedStripePriceId = trimToNull(stripePriceId);
        if (normalizedStripePriceId == null) {
            return null;
        }
        return membershipPlanMapper.selectOne(new QueryWrapper<MembershipPlan>()
                .eq("stripe_price_id", normalizedStripePriceId)
                .last("limit 1"));
    }

    private PaymentSubscription findSubscriptionByUserId(Long userId) {
        return paymentSubscriptionMapper.selectOne(new QueryWrapper<PaymentSubscription>()
                .eq("user_id", userId)
                .last("limit 1"));
    }

    private PaymentSubscription findSubscriptionByStripeSubscriptionId(String stripeSubscriptionId) {
        String normalizedStripeSubscriptionId = trimToNull(stripeSubscriptionId);
        if (normalizedStripeSubscriptionId == null) {
            return null;
        }
        return paymentSubscriptionMapper.selectOne(new QueryWrapper<PaymentSubscription>()
                .eq("stripe_subscription_id", normalizedStripeSubscriptionId)
                .last("limit 1"));
    }

    private PaymentOrder findOrderByStripeCheckoutSessionId(String stripeCheckoutSessionId) {
        String normalizedSessionId = trimToNull(stripeCheckoutSessionId);
        if (normalizedSessionId == null) {
            return null;
        }
        return paymentOrderMapper.selectOne(new QueryWrapper<PaymentOrder>()
                .eq("stripe_checkout_session_id", normalizedSessionId)
                .last("limit 1"));
    }

    private PaymentOrder findOrderByOrderNo(String orderNo) {
        String normalizedOrderNo = trimToNull(orderNo);
        if (normalizedOrderNo == null) {
            return null;
        }
        return paymentOrderMapper.selectOne(new QueryWrapper<PaymentOrder>()
                .eq("order_no", normalizedOrderNo)
                .last("limit 1"));
    }

    private PaymentOrder findOrderByStripeInvoiceId(String stripeInvoiceId) {
        String normalizedInvoiceId = trimToNull(stripeInvoiceId);
        if (normalizedInvoiceId == null) {
            return null;
        }
        return paymentOrderMapper.selectOne(new QueryWrapper<PaymentOrder>()
                .eq("stripe_invoice_id", normalizedInvoiceId)
                .last("limit 1"));
    }

    private PaymentOrder findLatestCheckoutOrderWithoutInvoice(String stripeSubscriptionId) {
        String normalizedSubscriptionId = trimToNull(stripeSubscriptionId);
        if (normalizedSubscriptionId == null) {
            return null;
        }
        return paymentOrderMapper.selectOne(new QueryWrapper<PaymentOrder>()
                .eq("stripe_subscription_id", normalizedSubscriptionId)
                .eq("order_type", ORDER_TYPE_SUBSCRIPTION_CHECKOUT)
                .isNull("stripe_invoice_id")
                .orderByDesc("created_at")
                .last("limit 1"));
    }

    private PaymentWebhookEvent findWebhookEventByStripeEventId(String stripeEventId) {
        String normalizedEventId = trimToNull(stripeEventId);
        if (normalizedEventId == null) {
            return null;
        }
        return paymentWebhookEventMapper.selectOne(new QueryWrapper<PaymentWebhookEvent>()
                .eq("stripe_event_id", normalizedEventId)
                .last("limit 1"));
    }

    private RequestOptions requestOptions() {
        return requestOptions(null);
    }

    private RequestOptions requestOptions(String idempotencyKey) {
        RequestOptions.RequestOptionsBuilder builder = RequestOptions.builder()
                .setApiKey(paymentProperties.getSecretKey());
        if (StringUtils.hasText(idempotencyKey)) {
            builder.setIdempotencyKey(idempotencyKey);
        }
        return builder.build();
    }

    private MkApiResponse<PaymentCheckoutSessionDTO> paymentNotReadyResponse() {
        if (!paymentProperties.isEnabled()) {
            return MkApiResponse.error(503, "Payment module is disabled");
        }
        if (!paymentProperties.hasSecretKey()) {
            return MkApiResponse.error(503, "Stripe secret key is not configured");
        }
        if (!paymentProperties.hasWebhookSecret()) {
            return MkApiResponse.error(503, "Stripe webhook secret is not configured");
        }
        return MkApiResponse.error(503, "Payment module is not ready");
    }

    private <T> MkApiResponse<T> paymentApiNotConfiguredResponse() {
        if (!paymentProperties.isEnabled()) {
            return MkApiResponse.error(503, "Payment module is disabled");
        }
        if (!paymentProperties.hasSecretKey()) {
            return MkApiResponse.error(503, "Stripe secret key is not configured");
        }
        return MkApiResponse.error(503, "Payment API is not ready");
    }

    private boolean isApiConfigured() {
        return paymentProperties.isEnabled() && paymentProperties.hasSecretKey();
    }

    private boolean isOpenSubscriptionStatus(String status) {
        String normalizedStatus = trimToNull(status);
        return normalizedStatus != null && OPEN_SUBSCRIPTION_STATUSES.contains(normalizedStatus.toLowerCase());
    }

    private boolean isActiveEntitlementStatus(String status) {
        String normalizedStatus = trimToNull(status);
        return normalizedStatus != null && ACTIVE_ENTITLEMENT_STATUSES.contains(normalizedStatus.toLowerCase());
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

    private String validateRedirectUrl(String url, String fieldLabel) {
        String normalizedUrl = trimToNull(url);
        if (normalizedUrl == null) {
            return fieldLabel + " is required";
        }
        if (!(normalizedUrl.startsWith("http://") || normalizedUrl.startsWith("https://"))) {
            return fieldLabel + " must be an absolute http(s) URL";
        }
        return null;
    }

    private String generateOrderNo() {
        return "PO-" + DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS").format(LocalDateTime.now())
                + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private String generateIdempotencyKey() {
        return "mk-" + UUID.randomUUID();
    }

    private String resolveStripeSubscriptionId(Invoice invoice) {
        if (invoice == null || invoice.getParent() == null || invoice.getParent().getSubscriptionDetails() == null) {
            return null;
        }
        return trimToNull(invoice.getParent().getSubscriptionDetails().getSubscription());
    }

    private String resolveInvoicePaymentIntentId(Invoice invoice) {
        if (invoice == null || invoice.getPayments() == null || invoice.getPayments().getData() == null || invoice.getPayments().getData().isEmpty()) {
            return null;
        }
        if (invoice.getPayments().getData().get(0).getPayment() == null) {
            return null;
        }
        return trimToNull(invoice.getPayments().getData().get(0).getPayment().getPaymentIntent());
    }

    private LocalDateTime resolveSubscriptionCurrentPeriodStart(Subscription stripeSubscription) {
        if (stripeSubscription == null || stripeSubscription.getItems() == null || stripeSubscription.getItems().getData() == null || stripeSubscription.getItems().getData().isEmpty()) {
            return null;
        }
        return toLocalDateTime(stripeSubscription.getItems().getData().get(0).getCurrentPeriodStart());
    }

    private LocalDateTime resolveSubscriptionCurrentPeriodEnd(Subscription stripeSubscription) {
        if (stripeSubscription == null || stripeSubscription.getItems() == null || stripeSubscription.getItems().getData() == null || stripeSubscription.getItems().getData().isEmpty()) {
            return null;
        }
        return toLocalDateTime(stripeSubscription.getItems().getData().get(0).getCurrentPeriodEnd());
    }
    private LocalDateTime toLocalDateTime(Long epochSeconds) {
        if (epochSeconds == null || epochSeconds <= 0) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneOffset.UTC);
    }

    private Long parseLong(String value) {
        String normalizedValue = trimToNull(value);
        if (normalizedValue == null) {
            return null;
        }
        try {
            return Long.parseLong(normalizedValue);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Long nullSafeLong(Long value) {
        return value == null ? 0L : value;
    }

    private String objectToString(Object value) {
        return value == null ? null : value.toString();
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String defaultText(String value, String fallback) {
        return trimToNull(value) == null ? fallback : value;
    }

    private String abbreviate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
