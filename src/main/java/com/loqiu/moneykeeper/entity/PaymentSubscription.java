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
@TableName("payment_subscription")
public class PaymentSubscription {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("plan_id")
    private Long planId;

    @TableField("plan_code")
    private String planCode;

    @TableField("plan_name")
    private String planName;

    private String currency;

    @TableField("amount_minor")
    private Long amountMinor;

    @TableField("billing_interval")
    private String billingInterval;

    private String status;

    @TableField("stripe_customer_id")
    private String stripeCustomerId;

    @TableField("stripe_subscription_id")
    private String stripeSubscriptionId;

    @TableField("stripe_price_id")
    private String stripePriceId;

    @TableField("current_period_start")
    private LocalDateTime currentPeriodStart;

    @TableField("current_period_end")
    private LocalDateTime currentPeriodEnd;

    @TableField("cancel_at_period_end")
    private Integer cancelAtPeriodEnd;

    @TableField("canceled_at")
    private LocalDateTime canceledAt;

    @TableField("latest_order_id")
    private Long latestOrderId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}