package com.loqiu.moneykeeper.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("categories")
public class Category {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @NonNull
    private String name;
    
    @NonNull
    private String icon;
    
    @NonNull
    private String color;
    
    @NonNull
    private String type;
    
    @NonNull
    @TableField("user_id")
    private Long userId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic(value = "0", delval = "1")
    private Integer deletedAt;

    private LocalDateTime deletedTime;
} 