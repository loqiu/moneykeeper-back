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
@TableName("membership_plan")
public class MembershipPlan {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;
    private String name;
    private String description;

    @TableField("stripe_price_id")
    private String stripePriceId;

    private String currency;

    @TableField("amount_minor")
    private Long amountMinor;

    @TableField("billing_interval")
    private String billingInterval;

    private Integer active;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}