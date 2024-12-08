package com.loqiu.moneykeeper.service.impl;

import com.loqiu.moneykeeper.enums.MessageType;
import com.loqiu.moneykeeper.service.NotificationService;
import com.loqiu.moneykeeper.service.SseEmitterService;
import com.loqiu.moneykeeper.vo.NotificationMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {


    @Autowired
    private SseEmitterService sseEmitterService;

    @Override
    public void sendMessage(Long userId, String title, String message, MessageType type) {
        NotificationMessage notificationMessage = NotificationMessage.builder()
                .title(title)
                .message(message)
                .type(type)
                .timestamp(System.currentTimeMillis())
                .build();
        sseEmitterService.sendMessage(userId, notificationMessage);
    }

    @Override
    public void sendSuccessMessage(Long userId, String title, String message) {
        sendMessage(userId, title, message, MessageType.SUCCESS);
    }

    @Override
    public void sendErrorMessage(Long userId, String title, String message) {
        sendMessage(userId, title, message, MessageType.ERROR);
    }

    @Override
    public void sendInfoMessage(Long userId, String title, String message) {
        sendMessage(userId, title, message, MessageType.INFO);
    }

    @Override
    public void sendWarningMessage(Long userId, String title, String message) {
        sendMessage(userId, title, message, MessageType.WARNING);
    }

    @Override
    public void broadcastMessage(String title, String message, MessageType type) {
        NotificationMessage notificationMessage = NotificationMessage.builder()
                .title(title)
                .message(message)
                .type(type)
                .timestamp(System.currentTimeMillis())
                .build();
        sseEmitterService.sendMessageToAll(notificationMessage);
    }

    @Override
    public void broadcastSuccessMessage(String title, String message) {
        broadcastMessage(title, message, MessageType.SUCCESS);
    }

    @Override
    public void broadcastErrorMessage(String title, String message) {
        broadcastMessage(title, message, MessageType.ERROR);
    }

    @Override
    public void broadcastInfoMessage(String title, String message) {
        broadcastMessage(title, message, MessageType.INFO);
    }

    @Override
    public void broadcastWarningMessage(String title, String message) {
        broadcastMessage(title, message, MessageType.WARNING);
    }
} 