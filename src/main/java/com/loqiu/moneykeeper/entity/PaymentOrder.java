package com.loqiu.moneykeeper.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("payment_order")
public class PaymentOrder {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("order_no")
    private String orderNo;

    @TableField("user_id")
    private Long userId;

    @TableField("plan_id")
    private Long planId;

    @TableField("plan_code")
    private String planCode;

    @TableField("plan_name")
    private String planName;

    @TableField("order_type")
    private String orderType;

    private String status;
    private String currency;

    @TableField("amount_minor")
    private Long amountMinor;

    @TableField("stripe_checkout_session_id")
    private String stripeCheckoutSessionId;

    @TableField("stripe_payment_intent_id")
    private String stripePaymentIntentId;

    @TableField("stripe_invoice_id")
    private String stripeInvoiceId;

    @TableField("stripe_subscription_id")
    private String stripeSubscriptionId;

    @TableField("stripe_customer_id")
    private String stripeCustomerId;

    @TableField("idempotency_key")
    private String idempotencyKey;

    @TableField("paid_at")
    private LocalDateTime paidAt;

    @TableField("failure_message")
    private String failureMessage;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}