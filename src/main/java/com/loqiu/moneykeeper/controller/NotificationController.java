package com.loqiu.moneykeeper.controller;

import com.loqiu.moneykeeper.entity.User;
import com.loqiu.moneykeeper.exception.UnauthorizedException;
import com.loqiu.moneykeeper.service.NotificationService;
import com.loqiu.moneykeeper.service.SseEmitterService;
import com.loqiu.moneykeeper.service.UserService;
import com.loqiu.moneykeeper.util.JwtUtil;
import com.loqiu.moneykeeper.vo.NotificationMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping(value = "/subscribe/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String token) {
        logger.info("开始处理SSE订阅请求 - 用户ID: {},token:{}", userId,token);
        // 去掉Bearer
        token = token.substring(7);
        String userPin = jwtUtil.getUserIdFromToken(token);
        logger.info("sse订阅请求 - userPin: {}", userPin);
        User user = userService.findByUserPin(userPin);
        if (userId != user.getId()) {
            logger.warn("SSE订阅失败 - 用户ID不匹配 - 请求用户ID: {}, token中用户ID: {}", 
                userId, jwtUtil.getUserIdFromToken(token));
            throw new UnauthorizedException();
        }
        logger.info("SSE订阅成功 - 用户ID: {}", userId);
        return sseEmitterService.createEmitter(userId);
    }

    @PostMapping("/send/{userId}")
    public ResponseEntity<String> sendMessage(
            @PathVariable Long userId,
            @RequestBody NotificationMessage message) {
        logger.info("开始发送消息 - 用户ID: {}, 标题: {}, 类型: {}", 
            userId, message.getTitle(), message.getType());
        try {
            notificationService.sendMessage(userId, message.getTitle(), 
                message.getMessage(), message.getType());
            logger.info("消息发送成功 - 用户ID: {}, 标题: {}", userId, message.getTitle());
            return ResponseEntity.ok("消息已发送");
        } catch (Exception e) {
            logger.error("消息发送失败 - 用户ID: {}, 标题: {}, 错误: {}", 
                userId, message.getTitle(), e.getMessage());
            return ResponseEntity.internalServerError().body("消息发送失败");
        }
    }

    @PostMapping("/broadcast")
    public ResponseEntity<String> broadcast(@RequestBody NotificationMessage message) {
        logger.info("开始广播消息 - 标题: {}, 类型: {}", message.getTitle(), message.getType());
        try {
            notificationService.broadcastMessage(message.getTitle(), 
                message.getMessage(), message.getType());
            logger.info("广播消息发送成功 - 标题: {}", message.getTitle());
            return ResponseEntity.ok("广播消息已发送");
        } catch (Exception e) {
            logger.error("广播消息发送失败 - 标题: {}, 错误: {}", 
                message.getTitle(), e.getMessage());
            return ResponseEntity.internalServerError().body("广播消息发送失败");
        }
    }

    // 便捷方法
    @PostMapping("/send/{userId}/success")
    public ResponseEntity<String> sendSuccessMessage(
            @PathVariable Long userId,
            @RequestParam String title,
            @RequestParam String message) {
        logger.info("开始发送成功消息 - 用户ID: {}, 标题: {}", userId, title);
        try {
            notificationService.sendSuccessMessage(userId, title, message);
            logger.info("成功消息发送成功 - 用户ID: {}, 标题: {}", userId, title);
            return ResponseEntity.ok("成功消息已发送");
        } catch (Exception e) {
            logger.error("成功消息发送失败 - 用户ID: {}, 标题: {}, 错误: {}", 
                userId, title, e.getMessage());
            return ResponseEntity.internalServerError().body("成功消息发送失败");
        }
    }

    @PostMapping("/send/{userId}/error")
    public ResponseEntity<String> sendErrorMessage(
            @PathVariable Long userId,
            @RequestParam String title,
            @RequestParam String message) {
        logger.info("开始发送错误消息 - 用户ID: {}, 标题: {}", userId, title);
        try {
            notificationService.sendErrorMessage(userId, title, message);
            logger.info("错误消息发送成功 - 用户ID: {}, 标题: {}", userId, title);
            return ResponseEntity.ok("错误消息已发送");
        } catch (Exception e) {
            logger.error("错误消息发送失败 - 用户ID: {}, 标题: {}, 错误: {}", 
                userId, title, e.getMessage());
            return ResponseEntity.internalServerError().body("错误消息发送失败");
        }
    }

    @PostMapping("/broadcast/success")
    public ResponseEntity<String> broadcastSuccessMessage(
            @RequestParam String title,
            @RequestParam String message) {
        logger.info("开始广播成功消息 - 标题: {}", title);
        try {
            notificationService.broadcastSuccessMessage(title, message);
            logger.info("成功消息广播成功 - 标题: {}", title);
            return ResponseEntity.ok("成功广播消息已发送");
        } catch (Exception e) {
            logger.error("成功消息广播失败 - 标题: {}, 错误: {}", title, e.getMessage());
            return ResponseEntity.internalServerError().body("成功广播消息发送失败");
        }
    }

    @PostMapping("/broadcast/error")
    public ResponseEntity<String> broadcastErrorMessage(
            @RequestParam String title,
            @RequestParam String message) {
        logger.info("开始广播错误消息 - 标题: {}", title);
        try {
            notificationService.broadcastErrorMessage(title, message);
            logger.info("错误消息广播成功 - 标题: {}", title);
            return ResponseEntity.ok("错误广播消息已发送");
        } catch (Exception e) {
            logger.error("错误消息广播失败 - 标题: {}, 错误: {}", title, e.getMessage());
            return ResponseEntity.internalServerError().body("错误广播消息发送失败");
        }
    }
} 