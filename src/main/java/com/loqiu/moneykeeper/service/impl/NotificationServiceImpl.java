package com.loqiu.moneykeeper.service.impl;

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
        MessageType safeType = type == null ? MessageType.INFO : type;
        persistLog(userId, title, message, safeType);
        sseEmitterService.sendMessage(userId, buildRealtimeMessage(title, message, safeType));
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
        MessageType safeType = type == null ? MessageType.INFO : type;
        List<User> users = userMapper.selectList(new QueryWrapper<User>().eq("deleted_at", 0));
        for (User user : users) {
            persistLog(user.getId(), title, message, safeType);
        }
        sseEmitterService.sendMessageToAll(buildRealtimeMessage(title, message, safeType));
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
        NotificationLog log = NotificationLog.builder()
                .userId(userId)
                .title(title)
                .message(message)
                .type(type.getType())
                .channel("sse")
                .status("sent")
                .isRead(false)
                .build();
        notificationLogMapper.insert(log);
        return log;
    }

    private NotificationMessage buildRealtimeMessage(String title, String message, MessageType type) {
        return NotificationMessage.builder()
                .title(title)
                .message(message)
                .type(type)
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
                .channel(log.getChannel())
                .status(log.getStatus())
                .read(Boolean.TRUE.equals(log.getIsRead()))
                .readAt(log.getReadAt())
                .createdAt(log.getCreatedAt())
                .updatedAt(log.getUpdatedAt())
                .build();
    }
}
