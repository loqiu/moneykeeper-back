package com.loqiu.moneykeeper.controller;

import com.loqiu.moneykeeper.exception.BadRequestException;
import com.loqiu.moneykeeper.exception.ForbiddenException;
import com.loqiu.moneykeeper.service.NotificationService;
import com.loqiu.moneykeeper.service.SseEmitterService;
import com.loqiu.moneykeeper.util.RequestAuthUtil;
import com.loqiu.moneykeeper.vo.NotificationMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(
        originPatterns = {"*"},
        allowCredentials = "true",
        allowedHeaders = "*",
        exposedHeaders = "*",
        maxAge = 3600
)
public class NotificationController {

    private static final Logger logger = LogManager.getLogger(NotificationController.class);

    @Autowired
    private SseEmitterService sseEmitterService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping(value = "/subscribe/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@PathVariable Long userId, HttpServletRequest request) {
        Long currentUserId = RequestAuthUtil.requireCurrentUserId(request);
        logger.info("Processing SSE subscribe request - targetUserId: {}, currentUserId: {}", userId, currentUserId);
        requireSelfOrAdmin(request, userId);
        return sseEmitterService.createEmitter(userId);
    }

    @PostMapping("/send/{userId}")
    public ResponseEntity<String> sendMessage(@PathVariable Long userId,
                                              @RequestBody NotificationMessage message,
                                              HttpServletRequest request) {
        logger.info("Sending notification - targetUserId: {}, currentUserId: {}, title: {}",
                userId, RequestAuthUtil.getCurrentUserId(request), message == null ? null : message.getTitle());
        requireSelfOrAdmin(request, userId);
        NotificationMessage validMessage = requireNotificationMessage(message);
        notificationService.sendMessage(userId, validMessage.getTitle(), validMessage.getMessage(), validMessage.getType());
        return ResponseEntity.ok("Message sent");
    }

    @PostMapping("/broadcast")
    public ResponseEntity<String> broadcast(@RequestBody NotificationMessage message, HttpServletRequest request) {
        logger.info("Broadcast request - currentUserId: {}, title: {}",
                RequestAuthUtil.getCurrentUserId(request), message == null ? null : message.getTitle());
        requireAdmin(request);
        NotificationMessage validMessage = requireNotificationMessage(message);
        notificationService.broadcastMessage(validMessage.getTitle(), validMessage.getMessage(), validMessage.getType());
        return ResponseEntity.ok("Broadcast sent");
    }

    @PostMapping("/send/{userId}/success")
    public ResponseEntity<String> sendSuccessMessage(@PathVariable Long userId,
                                                     @RequestParam String title,
                                                     @RequestParam String message,
                                                     HttpServletRequest request) {
        logger.info("Sending success notification - targetUserId: {}, currentUserId: {}", userId, RequestAuthUtil.getCurrentUserId(request));
        requireSelfOrAdmin(request, userId);
        notificationService.sendSuccessMessage(userId, requireText(title, "Title is required"), requireText(message, "Message is required"));
        return ResponseEntity.ok("Success message sent");
    }

    @PostMapping("/send/{userId}/error")
    public ResponseEntity<String> sendErrorMessage(@PathVariable Long userId,
                                                   @RequestParam String title,
                                                   @RequestParam String message,
                                                   HttpServletRequest request) {
        logger.info("Sending error notification - targetUserId: {}, currentUserId: {}", userId, RequestAuthUtil.getCurrentUserId(request));
        requireSelfOrAdmin(request, userId);
        notificationService.sendErrorMessage(userId, requireText(title, "Title is required"), requireText(message, "Message is required"));
        return ResponseEntity.ok("Error message sent");
    }

    @PostMapping("/broadcast/success")
    public ResponseEntity<String> broadcastSuccessMessage(@RequestParam String title,
                                                          @RequestParam String message,
                                                          HttpServletRequest request) {
        logger.info("Broadcasting success notification - currentUserId: {}", RequestAuthUtil.getCurrentUserId(request));
        requireAdmin(request);
        notificationService.broadcastSuccessMessage(requireText(title, "Title is required"), requireText(message, "Message is required"));
        return ResponseEntity.ok("Success broadcast sent");
    }

    @PostMapping("/broadcast/error")
    public ResponseEntity<String> broadcastErrorMessage(@RequestParam String title,
                                                        @RequestParam String message,
                                                        HttpServletRequest request) {
        logger.info("Broadcasting error notification - currentUserId: {}", RequestAuthUtil.getCurrentUserId(request));
        requireAdmin(request);
        notificationService.broadcastErrorMessage(requireText(title, "Title is required"), requireText(message, "Message is required"));
        return ResponseEntity.ok("Error broadcast sent");
    }

    private NotificationMessage requireNotificationMessage(NotificationMessage message) {
        if (message == null) {
            throw new BadRequestException("Request body is required");
        }
        String title = requireText(message.getTitle(), "Title is required");
        String content = requireText(message.getMessage(), "Message is required");
        if (message.getType() == null) {
            throw new BadRequestException("Notification type is required");
        }
        return NotificationMessage.builder()
                .title(title)
                .message(content)
                .type(message.getType())
                .timestamp(message.getTimestamp())
                .build();
    }

    private String requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new BadRequestException(message);
        }
        return value.trim();
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
            throw new ForbiddenException("You do not have permission to access this notification resource");
        }
    }
}