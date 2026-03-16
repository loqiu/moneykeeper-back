package com.loqiu.moneykeeper.service;

import com.loqiu.moneykeeper.dto.NotificationLogDTO;
import com.loqiu.moneykeeper.enums.MessageType;

import java.util.List;
import java.util.Map;

public interface NotificationService {
    void sendMessage(Long userId, String title, String message, MessageType type);
    void sendMessage(Long userId, String title, String message, MessageType type, String eventKey, Map<String, Object> payload);
    void sendSuccessMessage(Long userId, String title, String message);
    void sendErrorMessage(Long userId, String title, String message);
    void sendInfoMessage(Long userId, String title, String message);
    void sendWarningMessage(Long userId, String title, String message);
    void sendInfoMessage(Long userId, String title, String message, String eventKey, Map<String, Object> payload);
    void sendErrorMessage(Long userId, String title, String message, String eventKey, Map<String, Object> payload);
    void sendWarningMessage(Long userId, String title, String message, String eventKey, Map<String, Object> payload);

    void broadcastMessage(String title, String message, MessageType type);
    void broadcastMessage(String title, String message, MessageType type, String eventKey, Map<String, Object> payload);
    void broadcastSuccessMessage(String title, String message);
    void broadcastErrorMessage(String title, String message);
    void broadcastInfoMessage(String title, String message);
    void broadcastWarningMessage(String title, String message);

    List<NotificationLogDTO> listLogs(Long userId, Boolean unreadOnly, MessageType type, int limit);
    NotificationLogDTO getLog(Long userId, Long notificationId);
    NotificationLogDTO markAsRead(Long userId, Long notificationId);
    long markAllAsRead(Long userId, MessageType type);
    long countUnread(Long userId, MessageType type);
}
