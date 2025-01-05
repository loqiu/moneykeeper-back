package com.loqiu.moneykeeper.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private Long userId;
    private String userPin;
    private String username;
    private String token;
} 