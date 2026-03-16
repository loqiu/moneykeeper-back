package com.loqiu.moneykeeper.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.loqiu.moneykeeper.dto.NotificationLogDTO;
import com.loqiu.moneykeeper.entity.NotificationLog;
import com.loqiu.moneykeeper.entity.User;
import com.loqiu.moneykeeper.enums.MessageType;
import com.loqiu.moneykeeper.exception.ResourceNotFoundException;
import com.loqiu.moneykeeper.mapper.NotificationLogMapper;
import com.loqiu.moneykeeper.mapper.UserMapper;
import com.loqiu.moneykeeper.service.NotificationService;
import com.loqiu.moneykeeper.service.SseEmitterService;
import com.loqiu.moneykeeper.vo.NotificationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private SseEmitterService sseEmitterService;

    @Autowired
    private NotificationLogMapper notificationLogMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public void sendMessage(Long userId, String title, String message, MessageType type) {
        sendMessage(userId, title, message, type, null, Map.of());
    }

    @Override
    public void sendMessage(Long userId,
                            String title,
                            String message,
                            MessageType type,
                            String eventKey,
                            Map<String, Object> payload) {
        MessageType safeType = type == null ? MessageType.INFO : type;
        Map<String, Object> safePayload = normalizePayload(payload);
        persistLog(userId, title, message, safeType, eventKey, safePayload);
        sseEmitterService.sendMessage(userId, buildRealtimeMessage(title, message, safeType, eventKey, safePayload));
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
    public void sendInfoMessage(Long userId, String title, String message, String eventKey, Map<String, Object> payload) {
        sendMessage(userId, title, message, MessageType.INFO, eventKey, payload);
    }

    @Override
    public void sendWarningMessage(Long userId, String title, String message) {
        sendMessage(userId, title, message, MessageType.WARNING);
    }

    @Override
    public void sendWarningMessage(Long userId, String title, String message, String eventKey, Map<String, Object> payload) {
        sendMessage(userId, title, message, MessageType.WARNING, eventKey, payload);
    }

    @Override
    public void sendErrorMessage(Long userId, String title, String message, String eventKey, Map<String, Object> payload) {
        sendMessage(userId, title, message, MessageType.ERROR, eventKey, payload);
    }

    @Override
    public void broadcastMessage(String title, String message, MessageType type) {
        broadcastMessage(title, message, type, null, Map.of());
    }

    @Override
    public void broadcastMessage(String title,
                                 String message,
                                 MessageType type,
                                 String eventKey,
                                 Map<String, Object> payload) {
        MessageType safeType = type == null ? MessageType.INFO : type;
        Map<String, Object> safePayload = normalizePayload(payload);
        List<User> users = userMapper.selectList(new QueryWrapper<User>().eq("deleted_at", 0));
        for (User user : users) {
            persistLog(user.getId(), title, message, safeType, eventKey, safePayload);
        }
        sseEmitterService.sendMessageToAll(buildRealtimeMessage(title, message, safeType, eventKey, safePayload));
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

    @Override
    public List<NotificationLogDTO> listLogs(Long userId, Boolean unreadOnly, MessageType type, int limit) {
        QueryWrapper<NotificationLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .orderByDesc("created_at")
                .orderByDesc("id")
                .last("limit " + limit);
        if (Boolean.TRUE.equals(unreadOnly)) {
            queryWrapper.eq("is_read", 0);
        }
        if (type != null) {
            queryWrapper.eq("type", type.getType());
        }
        return notificationLogMapper.selectList(queryWrapper).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public NotificationLogDTO getLog(Long userId, Long notificationId) {
        return toDto(requireLog(userId, notificationId));
    }

    @Override
    public NotificationLogDTO markAsRead(Long userId, Long notificationId) {
        NotificationLog log = requireLog(userId, notificationId);
        if (!Boolean.TRUE.equals(log.getIsRead())) {
            log.setIsRead(true);
            log.setReadAt(LocalDateTime.now());
            notificationLogMapper.updateById(log);
        }
        return toDto(log);
    }

    @Override
    public long markAllAsRead(Long userId, MessageType type) {
        UpdateWrapper<NotificationLog> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("user_id", userId)
                .eq("is_read", 0)
                .set("is_read", 1)
                .set("read_at", LocalDateTime.now())
                .set("updated_at", LocalDateTime.now());
        if (type != null) {
            updateWrapper.eq("type", type.getType());
        }
        return notificationLogMapper.update(null, updateWrapper);
    }

    @Override
    public long countUnread(Long userId, MessageType type) {
        QueryWrapper<NotificationLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("is_read", 0);
        if (type != null) {
            queryWrapper.eq("type", type.getType());
        }
        Long count = notificationLogMapper.selectCount(queryWrapper);
        return count == null ? 0L : count;
    }

    private NotificationLog requireLog(Long userId, Long notificationId) {
        QueryWrapper<NotificationLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", notificationId)
                .eq("user_id", userId);
        NotificationLog log = notificationLogMapper.selectOne(queryWrapper);
        if (log == null) {
            throw new ResourceNotFoundException("Notification log not found");
        }
        return log;
    }

    private NotificationLog persistLog(Long userId, String title, String message, MessageType type) {
        return persistLog(userId, title, message, type, null, Map.of());
    }

    private NotificationLog persistLog(Long userId,
                                       String title,
                                       String message,
                                       MessageType type,
                                       String eventKey,
                                       Map<String, Object> payload) {
        NotificationLog log = NotificationLog.builder()
                .userId(userId)
                .title(title)
                .message(message)
                .type(type.getType())
                .eventKey(eventKey)
                .payloadJson(toPayloadJson(payload))
                .channel("sse")
                .status("sent")
                .isRead(false)
                .build();
        notificationLogMapper.insert(log);
        return log;
    }

    private NotificationMessage buildRealtimeMessage(String title,
                                                     String message,
                                                     MessageType type,
                                                     String eventKey,
                                                     Map<String, Object> payload) {
        return NotificationMessage.builder()
                .title(title)
                .message(message)
                .type(type)
                .eventKey(eventKey)
                .payload(normalizePayload(payload))
                .timestamp(System.currentTimeMillis())
                .build();
    }

    private NotificationLogDTO toDto(NotificationLog log) {
        return NotificationLogDTO.builder()
                .id(log.getId())
                .userId(log.getUserId())
                .title(log.getTitle())
                .message(log.getMessage())
                .type(MessageType.fromString(log.getType()))
                .eventKey(log.getEventKey())
                .payload(parsePayload(log.getPayloadJson()))
                .channel(log.getChannel())
                .status(log.getStatus())
                .read(Boolean.TRUE.equals(log.getIsRead()))
                .readAt(log.getReadAt())
                .createdAt(log.getCreatedAt())
                .updatedAt(log.getUpdatedAt())
                .build();
    }

    private String toPayloadJson(Map<String, Object> payload) {
        Map<String, Object> safePayload = normalizePayload(payload);
        return safePayload.isEmpty() ? "{}" : JSON.toJSONString(safePayload);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parsePayload(String payloadJson) {
        if (payloadJson == null || payloadJson.isBlank()) {
            return Map.of();
        }
        Map<String, Object> parsed = JSON.parseObject(payloadJson, Map.class);
        return parsed == null ? Map.of() : Map.copyOf(parsed);
    }

    private Map<String, Object> normalizePayload(Map<String, Object> payload) {
        if (payload == null || payload.isEmpty()) {
            return Map.of();
        }
        return Map.copyOf(payload);
    }
}
