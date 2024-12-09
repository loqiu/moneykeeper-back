package com.loqiu.moneykeeper.enums;

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

    public String getType() {
        return type;
    }

    public static MessageType fromString(String text) {
        for (MessageType type : MessageType.values()) {
            if (type.type.equalsIgnoreCase(text)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }
} 