package com.loqiu.moneykeeper.service;

import com.loqiu.moneykeeper.vo.NotificationMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseEmitterService {
    SseEmitter createEmitter(Long userId);
    void removeEmitter(Long userId);
    void sendMessage(Long userId, NotificationMessage message);
    void sendMessageToAll(NotificationMessage message);
} 