package com.loqiu.moneykeeper.service.impl;

import com.alibaba.fastjson.JSON;
import com.loqiu.moneykeeper.enums.MessageType;
import com.loqiu.moneykeeper.service.SseEmitterService;
import com.loqiu.moneykeeper.vo.NotificationMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SseEmitterServiceImpl
 */
@Service
public class SseEmitterServiceImpl implements SseEmitterService {
    
    private static final Logger logger = LogManager.getLogger(SseEmitterServiceImpl.class);

    private final ConcurrentHashMap<Long, SseEmitter> emitterMap;

    @Autowired
    public SseEmitterServiceImpl(ConcurrentHashMap<Long, SseEmitter> emitterMap) {
        this.emitterMap = emitterMap;
    }

    @Override
    public SseEmitter createEmitter(Long userId) {
        logger.info("创建SSE连接，用户ID: {}", userId);
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        
        // 设置超时回调
        emitter.onTimeout(() -> {
            logger.warn("SSE连接超时，用户ID: {}", userId);
            emitter.complete();
            emitterMap.remove(userId);
        });
        
        // 设置完成回调
        emitter.onCompletion(() -> {
            logger.info("SSE连接完成，用户ID: {}", userId);
            emitterMap.remove(userId);
        });
        
        // 设置错误回调
        emitter.onError(e -> {
            logger.error("SSE连接发生错误，用户ID: {}, 错误: {}", userId, e.getMessage());
            emitter.complete();
            emitterMap.remove(userId);
        });
        
        emitterMap.put(userId, emitter);
        
        // 发送初始连接成功消息
        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data(NotificationMessage.builder()
                            .title("连接成功")
                            .type(MessageType.CONNECT)
                            .message("连接成功")
                            .timestamp(System.currentTimeMillis()).build()));
            // 立即发送第一次心跳
            sendHeartbeat(emitter);
            logger.info("SSE连接已成功创建，用户ID: {}", userId);
        } catch (IOException e) {
            logger.error("发送初始连接消息失败，用户ID: {}, 错误: {}", userId, e.getMessage());
            emitter.completeWithError(e);
        }
        
        return emitter;
    }

    // 每30秒执行一次心跳
    @Scheduled(fixedRate = 30000)
    public void sendHeartbeatToAll() {
//        logger.info("开始发送心跳消息到所有连接，当前连接数: {}", emitterMap.size());
        emitterMap.forEach((userId, emitter) -> {
            try {
                sendHeartbeat(emitter);
//                logger.info("心跳消息发送成功 - 用户ID: {}", userId);
            } catch (IOException e) {
                logger.error("发送心跳消息失败，用户ID: {}, 错误: {}", userId, e.getMessage());
                emitterMap.remove(userId);
                emitter.completeWithError(e);
            }
        });
    }

    private void sendHeartbeat(SseEmitter emitter) throws IOException {
        emitter.send(SseEmitter.event()
                .name("heartbeat")
                .data(NotificationMessage.builder()
                        .title("心跳连接")
                        .type(MessageType.HEARTBEAT)
                        .message("心跳连接")
                        .timestamp(System.currentTimeMillis()).build()));
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
                        .data(JSON.toJSONString(message)));
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

    @Override
    public List<Long> getConnectedUsers() {
        return new ArrayList<>(emitterMap.keySet());
    }

    @Override
    public Map<Long, SseEmitter> getAllEmitters() {
        return new HashMap<>(emitterMap);
    }

    @Override
    public int getConnectedCount() {
        return emitterMap.size();
    }

    @Override
    public boolean isUserConnected(Long userId) {
        return emitterMap.containsKey(userId);
    }
} 