package com.loqiu.moneykeeper.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MessageType {
    SUCCESS("success"),
    WARNING("warning"),
    INFO("info"),
    ERROR("error"),
    HEARTBEAT("heartbeat"),
    CONNECT("connect");

    private final String type;

    MessageType(String type) {
        this.type = type;
    }

    @JsonValue
    public String getType() {
        return type;
    }

    @JsonCreator
    public static MessageType fromString(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        for (MessageType type : MessageType.values()) {
            if (type.type.equalsIgnoreCase(text)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unsupported message type: " + text);
    }
}