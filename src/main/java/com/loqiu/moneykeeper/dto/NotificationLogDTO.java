package com.loqiu.moneykeeper.dto;

import com.loqiu.moneykeeper.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationLogDTO {
    private Long id;
    private Long userId;
    private String title;
    private String message;
    private MessageType type;
    private String eventKey;
    private Map<String, Object> payload;
    private String channel;
    private String status;
    private Boolean read;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
