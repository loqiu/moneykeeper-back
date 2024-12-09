package com.loqiu.moneykeeper.controller;

import com.loqiu.moneykeeper.service.SseEmitterService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
/**
 * EmitterManageController
 * 管理SSE连接的控制器
 */
@RestController
@RequestMapping("/api/notifications/manage")
public class EmitterManageController {
    
    private static final Logger logger = LogManager.getLogger(EmitterManageController.class);

    @Autowired
    private SseEmitterService sseEmitterService;

    @GetMapping("/connections")
    public ResponseEntity<Map<String, Object>> getConnectionInfo() {
        logger.info("获取SSE连接信息");
        Map<String, Object> info = new HashMap<>();
        info.put("connectedUsers", sseEmitterService.getConnectedUsers());
        info.put("totalConnections", sseEmitterService.getConnectedCount());
        return ResponseEntity.ok(info);
    }

    @GetMapping("/check/{userId}")
    public ResponseEntity<Map<String, Object>> checkUserConnection(@PathVariable Long userId) {
        logger.info("检查用户连接状态 - 用户ID: {}", userId);
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("connected", sseEmitterService.isUserConnected(userId));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/disconnect/{userId}")
    public ResponseEntity<String> disconnectUser(@PathVariable Long userId) {
        logger.info("强制断开用户连接 - 用户ID: {}", userId);
        if (sseEmitterService.isUserConnected(userId)) {
            sseEmitterService.removeEmitter(userId);
            return ResponseEntity.ok("用户连接已断开");
        }
        return ResponseEntity.ok("用户未连接");
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getConnectionStats() {
        logger.info("获取SSE连接统计信息");
        Map<String, Object> stats = new HashMap<>();
        List<Long> connectedUsers = sseEmitterService.getConnectedUsers();
        
        stats.put("totalConnections", sseEmitterService.getConnectedCount());
        stats.put("connectedUsers", connectedUsers);
        stats.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(stats);
    }
} 