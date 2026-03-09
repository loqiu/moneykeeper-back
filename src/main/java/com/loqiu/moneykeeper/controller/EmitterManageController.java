package com.loqiu.moneykeeper.controller;

import com.loqiu.moneykeeper.exception.BadRequestException;
import com.loqiu.moneykeeper.exception.ForbiddenException;
import com.loqiu.moneykeeper.service.SseEmitterService;
import com.loqiu.moneykeeper.util.RequestAuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications/manage")
public class EmitterManageController {

    private static final Logger logger = LogManager.getLogger(EmitterManageController.class);

    @Autowired
    private SseEmitterService sseEmitterService;

    @GetMapping("/connections")
    public ResponseEntity<Map<String, Object>> getConnectionInfo(HttpServletRequest request) {
        logger.info("Getting SSE connection info - currentUserId: {}", RequestAuthUtil.getCurrentUserId(request));
        requireAdmin(request);
        Map<String, Object> info = new HashMap<>();
        info.put("connectedUsers", sseEmitterService.getConnectedUsers());
        info.put("totalConnections", sseEmitterService.getConnectedCount());
        return ResponseEntity.ok(info);
    }

    @GetMapping("/check/{userId}")
    public ResponseEntity<Map<String, Object>> checkUserConnection(@PathVariable Long userId, HttpServletRequest request) {
        logger.info("Checking SSE connection - targetUserId: {}, currentUserId: {}", userId, RequestAuthUtil.getCurrentUserId(request));
        requireSelfOrAdmin(request, userId);
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("connected", sseEmitterService.isUserConnected(userId));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/disconnect/{userId}")
    public ResponseEntity<String> disconnectUser(@PathVariable Long userId, HttpServletRequest request) {
        logger.info("Disconnecting SSE user - targetUserId: {}, currentUserId: {}", userId, RequestAuthUtil.getCurrentUserId(request));
        requireSelfOrAdmin(request, userId);
        if (sseEmitterService.isUserConnected(userId)) {
            sseEmitterService.removeEmitter(userId);
            return ResponseEntity.ok("Connection disconnected");
        }
        return ResponseEntity.ok("User is not connected");
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getConnectionStats(HttpServletRequest request) {
        logger.info("Getting SSE connection stats - currentUserId: {}", RequestAuthUtil.getCurrentUserId(request));
        requireAdmin(request);
        Map<String, Object> stats = new HashMap<>();
        List<Long> connectedUsers = sseEmitterService.getConnectedUsers();
        stats.put("totalConnections", sseEmitterService.getConnectedCount());
        stats.put("connectedUsers", connectedUsers);
        stats.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(stats);
    }

    private void requireAdmin(HttpServletRequest request) {
        if (!RequestAuthUtil.isAdmin(request)) {
            throw new ForbiddenException("Admin role is required");
        }
    }

    private void requireSelfOrAdmin(HttpServletRequest request, Long userId) {
        if (userId == null) {
            throw new BadRequestException("User id is required");
        }
        if (!RequestAuthUtil.isSelfOrAdmin(request, userId)) {
            throw new ForbiddenException("You do not have permission to access this SSE resource");
        }
    }
}