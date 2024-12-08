package com.loqiu.moneykeeper.service.impl;

import com.loqiu.moneykeeper.service.SseEmitterService;
import com.loqiu.moneykeeper.vo.NotificationMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SseEmitterServiceImpl implements SseEmitterService {
    
    private static final Logger logger = LogManager.getLogger(SseEmitterServiceImpl.class);

    // 使用ConcurrentHashMap存储每个用户的SSE连接
    @Autowired
    private ConcurrentHashMap<Long, SseEmitter> emitterMap;

    @Override
    public SseEmitter createEmitter(Long userId) {
        logger.info("创建SSE连接，用户ID: {}", userId);
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        
        // 设置超时回调
        emitter.onTimeout(() -> {
            emitter.complete();
            emitterMap.remove(userId);
        });
        
        // 设置完成回调
        emitter.onCompletion(() -> emitterMap.remove(userId));
        
        // 设置错误回调
        emitter.onError(e -> {
            emitter.complete();
            emitterMap.remove(userId);
        });
        
        emitterMap.put(userId, emitter);
        
        // 发送初始连接成功消息
        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("Connected successfully!"));
            logger.info("SSE连接已成功创建，用户ID: {}", userId);
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
        
        return emitter;
    }

    @Override
    public void removeEmitter(Long userId) {
        logger.info("移除SSE连接，用户ID: {}", userId);
        SseEmitter emitter = emitterMap.remove(userId);
        if (emitter != null) {
            emitter.complete();
        }
        logger.info("SSE连接已移除，用户ID: {}", userId);
    }

    @Override
    public void sendMessage(Long userId, NotificationMessage message) {
        logger.info("发送消息，用户ID: {}, 消息: {}", userId, message);
        message.setTimestamp(System.currentTimeMillis());
        SseEmitter emitter = emitterMap.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("message")
                        .data(message));
                logger.info("消息已发送，用户ID: {}, 消息: {}", userId, message);
            } catch (IOException e) {
                emitterMap.remove(userId);
                emitter.completeWithError(e);
            }
        }
    }

    @Override
    public void sendMessageToAll(NotificationMessage message) {
        logger.info("广播消息，消息: {}", message);
        message.setTimestamp(System.currentTimeMillis());
        emitterMap.forEach((userId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("message")
                        .data(message));
                logger.info("广播消息已发送，消息: {}", message);
            } catch (IOException e) {
                emitterMap.remove(userId);
                emitter.completeWithError(e);
            }
        });
    }
} 