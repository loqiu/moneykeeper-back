package com.loqiu.moneykeeper.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("moneykeeper")
public class MoneyKeeper {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @NonNull
    private Long userId;
    
    @NonNull
    private Long categoryId;
    
    @NonNull
    private String type;
    
    @NonNull
    private BigDecimal amount;
    
    @NonNull
    private LocalDate transactionDate;
    
    private String notes;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic(value = "0", delval = "1")
    private Integer deletedAt;

    private LocalDateTime deletedTime;
} 