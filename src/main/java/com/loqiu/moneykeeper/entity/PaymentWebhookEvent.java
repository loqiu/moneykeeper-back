package com.loqiu.moneykeeper.entity;

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
@TableName("payment_webhook_event")
public class PaymentWebhookEvent {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("stripe_event_id")
    private String stripeEventId;

    @TableField("event_type")
    private String eventType;

    private Integer processed;

    @TableField("payload_json")
    private String payloadJson;

    @TableField("related_order_id")
    private Long relatedOrderId;

    @TableField("related_subscription_id")
    private Long relatedSubscriptionId;

    @TableField("error_message")
    private String errorMessage;

    @TableField("received_at")
    private LocalDateTime receivedAt;

    @TableField("processed_at")
    private LocalDateTime processedAt;
}