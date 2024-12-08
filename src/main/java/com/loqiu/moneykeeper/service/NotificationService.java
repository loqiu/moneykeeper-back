package com.loqiu.moneykeeper.service;

import com.loqiu.moneykeeper.enums.MessageType;

public interface NotificationService {
    void sendMessage(Long userId, String title, String message, MessageType type);
    void sendSuccessMessage(Long userId, String title, String message);
    void sendErrorMessage(Long userId, String title, String message);
    void sendInfoMessage(Long userId, String title, String message);
    void sendWarningMessage(Long userId, String title, String message);
    
    void broadcastMessage(String title, String message, MessageType type);
    void broadcastSuccessMessage(String title, String message);
    void broadcastErrorMessage(String title, String message);
    void broadcastInfoMessage(String title, String message);
    void broadcastWarningMessage(String title, String message);
} 