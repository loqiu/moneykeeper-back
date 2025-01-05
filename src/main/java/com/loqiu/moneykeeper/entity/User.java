package com.loqiu.moneykeeper.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_pin")
    private String userPin;

    @NonNull
    private String username;
    
    @NonNull
    private String password;

    @TableField("email")
    private String email;

    @TableField("first_name")
    private String firstName;

    @TableField("last_name")
    private String lastName;

    @TableField("phone_number")
    private String phoneNumber;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    @TableLogic(value = "0", delval = "1")
    @TableField(fill = FieldFill.INSERT)
    private Integer deletedAt;

    private LocalDateTime deletedTime;

    @TableField("registration_completed_at")
    private LocalDateTime registrationCompletedAt;
} 