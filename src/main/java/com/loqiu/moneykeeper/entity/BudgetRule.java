package com.loqiu.moneykeeper.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("budget_rule")
public class BudgetRule {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("budget_id")
    private Long budgetId;

    @TableField("rule_type")
    private String ruleType;

    @TableField("threshold_percentage")
    private BigDecimal thresholdPercentage;

    private Integer enabled;

    @TableField("notification_title")
    private String notificationTitle;

    @TableField("notification_message")
    private String notificationMessage;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}