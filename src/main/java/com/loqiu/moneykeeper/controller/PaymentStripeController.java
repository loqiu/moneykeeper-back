package com.loqiu.moneykeeper.controller;

import com.alibaba.fastjson.JSON;
import com.loqiu.moneykeeper.dto.MkCheckoutSession;
import com.loqiu.moneykeeper.dto.MkPaymentIntentDTO;
import com.loqiu.moneykeeper.enums.PaymentErrorCode;
import com.loqiu.moneykeeper.response.MkApiResponse;
import com.loqiu.moneykeeper.service.PaymentStripeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "支付管理", description = "Stripe支付相关接口")
@RestController
@RequestMapping("/api/payment/stripe")
public class PaymentStripeController {

    private static final Logger logger = LogManager.getLogger(PaymentStripeController.class);

    @Autowired
    private PaymentStripeService paymentStripeService;

    @Operation(summary = "创建支付意向")
    @PostMapping("/create-payment-intent")
    public MkApiResponse<MkPaymentIntentDTO> createPaymentIntent(
            @RequestParam Long amount,
            @RequestParam String currency,
            @RequestParam("userId") Long userId) {
        logger.info("开始创建支付意向 - 用户ID: {}, 金额: {}, 货币: {}", userId, amount, currency);
        
        try {
            MkApiResponse<MkPaymentIntentDTO> result = paymentStripeService.createPaymentIntent(amount, currency);
            logger.info("支付意向创建成功 - 用户ID: {}, paymentIntentId: {}", userId, result.getData().getId());
            return result;
        } catch (Exception e) {
            logger.error("创建支付意向失败 - 用户ID: {}, 错误: {}", userId, e.getMessage());
            return MkApiResponse.error(PaymentErrorCode.CREATE_PAYMENT_INTENT_FAILED.getCode(),
                    PaymentErrorCode.CREATE_PAYMENT_INTENT_FAILED.getMessage());
        }
    }

    @Operation(summary = "确认支付")
    @PostMapping("/confirm-payment")
    public MkApiResponse<MkPaymentIntentDTO> confirmPayment(
            @RequestParam String paymentIntentId,
            @RequestAttribute("userId") Long userId) {
        logger.info("开始确认支付 - 用户ID: {}, 支付意向ID: {}", userId, paymentIntentId);
        
        try {
            MkApiResponse<MkPaymentIntentDTO> result = paymentStripeService.confirmPaymentIntent(paymentIntentId);
            logger.info("支付确认成功 - 用户ID: {}, 支付意向ID: {}", userId, paymentIntentId);
            return result;
        } catch (Exception e) {
            logger.error("支付确认失败 - 用户ID: {}, 支付意向ID: {}, 错误: {}", 
                userId, paymentIntentId, e.getMessage());
            return MkApiResponse.error(PaymentErrorCode.COMFIRM_PAYMENT_INTENT_FAILED.getCode(),
                    PaymentErrorCode.COMFIRM_PAYMENT_INTENT_FAILED.getMessage());
        }
    }

    @Operation(summary = "取消支付")
    @PostMapping("/cancel-payment")
    public MkApiResponse<MkPaymentIntentDTO> cancelPayment(
            @RequestParam String paymentIntentId,
            @RequestAttribute("userId") Long userId) {
        logger.info("开始取消支付 - 用户ID: {}, 支付意向ID: {}", userId, paymentIntentId);
        
        try {
            MkApiResponse<MkPaymentIntentDTO> result = paymentStripeService.cancelPaymentIntent(paymentIntentId);
            logger.info("支付取消成功 - 用户ID: {}, 支付意向ID: {}", userId, paymentIntentId);
            return result;
        } catch (Exception e) {
            logger.error("支付取消失败 - 用户ID: {}, 支付意向ID: {}, 错误: {}", 
                userId, paymentIntentId, e.getMessage());
            return MkApiResponse.error(PaymentErrorCode.CANCEL_PAYMENT_INTENT_FAILED.getCode(),
                    PaymentErrorCode.CANCEL_PAYMENT_INTENT_FAILED.getMessage());
        }
    }

    @Operation(summary = "取消支付")
    @PostMapping("/create-checkout-session")
    public MkApiResponse<MkPaymentIntentDTO> createSession(@RequestBody MkCheckoutSession payload) {
        logger.info("开始创建支付会话 - payload: {}", JSON.toJSONString(payload));
        try {
            MkApiResponse<MkPaymentIntentDTO> result = paymentStripeService.createCheckoutSession(payload);
            logger.info("创建支付会话成功 - paymentIntentId: {}", result.getData().getId());
            return result;
        } catch (Exception e) {
            logger.error("创建支付会话失败 - payload: {}, 错误: {}",
                    JSON.toJSONString(payload), e.getMessage());
            return MkApiResponse.error(PaymentErrorCode.CANCEL_PAYMENT_INTENT_FAILED.getCode(),
                    PaymentErrorCode.CANCEL_PAYMENT_INTENT_FAILED.getMessage());
        }
    }


    @Operation(summary = "处理Stripe Webhook")
    @PostMapping("/webhook")
    public MkApiResponse<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {
        logger.info("接收到Stripe Webhook请求 - signature: {}", signature);
        
        try {
            MkApiResponse<String> result = MkApiResponse.<String>builder()
                .code(200)
                .message("Webhook processed")
                .data("success")
                .build();
            logger.info("Webhook处理成功");
            return result;
        } catch (Exception e) {
            logger.error("Webhook处理失败 - 错误: {}", e.getMessage());
            return MkApiResponse.error(PaymentErrorCode.WEBHOOD_EVENT_NOT_FOUND.getCode(),
                    PaymentErrorCode.WEBHOOD_EVENT_NOT_FOUND.getMessage());
        }
    }
} 