package com.loqiu.moneykeeper.vo;

import com.loqiu.moneykeeper.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationMessage {
    private String message;
    private MessageType type;
    private Long timestamp;
    private String title;
    private String eventKey;
    private Map<String, Object> payload;
}
