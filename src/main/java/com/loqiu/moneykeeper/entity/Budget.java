package com.loqiu.moneykeeper.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("budget")
public class Budget {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("ledger_id")
    private Long ledgerId;

    @TableField("created_by_user_id")
    private Long createdByUserId;

    @TableField("category_id")
    private Long categoryId;

    private String name;

    @TableField("period_type")
    private String periodType;

    @TableField("budget_year")
    private Integer budgetYear;

    @TableField("budget_month")
    private Integer budgetMonth;

    private LocalDate startDate;

    private LocalDate endDate;

    private String type;

    private BigDecimal amount;

    private String notes;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic(value = "0", delval = "1")
    private Integer deletedAt;

    private LocalDateTime deletedTime;
}