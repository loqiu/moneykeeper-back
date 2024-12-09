package com.loqiu.moneykeeper.service;

import com.loqiu.moneykeeper.vo.NotificationMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

public interface SseEmitterService {
    SseEmitter createEmitter(Long userId);
    void removeEmitter(Long userId);
    void sendMessage(Long userId, NotificationMessage message);
    void sendMessageToAll(NotificationMessage message);
    
    // 新增的管理接口
    List<Long> getConnectedUsers();  // 获取所有在线用户ID
    Map<Long, SseEmitter> getAllEmitters();  // 获取所有连接
    int getConnectedCount();  // 获取当前连接数
    boolean isUserConnected(Long userId);  // 检查用户是否在线
} 