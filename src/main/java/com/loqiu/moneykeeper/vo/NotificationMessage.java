package com.loqiu.moneykeeper.vo;

import com.loqiu.moneykeeper.enums.MessageType;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationMessage {
    private String message;
    private MessageType type;
    private Long timestamp;
    private String title;
} 