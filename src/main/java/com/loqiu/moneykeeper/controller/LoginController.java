package com.loqiu.moneykeeper.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.loqiu.moneykeeper.entity.User;
import com.loqiu.moneykeeper.service.UserService;
import com.loqiu.moneykeeper.vo.LoginRequest;
import com.loqiu.moneykeeper.vo.LoginResponse;

@RestController
@RequestMapping("/api/auth")
public class LoginController {

    private static final Logger logger = LogManager.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        logger.info("Processing login request - username: {},password:{}", loginRequest.getUsername(),loginRequest.getPassword());
        
        // 参数校验
        if (loginRequest == null || loginRequest.getUsername() == null || loginRequest.getPassword() == null) {
            logger.error("Invalid login request - missing username or password");
            return ResponseEntity.badRequest().body("用户名和密码不能为空");
        }

        try {
            // 查找用户
            User user = userService.findByUsername(loginRequest.getUsername());
            
            if (user == null) {
                logger.warn("Login failed - user not found - username: {}", loginRequest.getUsername());
                return ResponseEntity.badRequest().body("用户不存在");
            }

            // 验证密码
            if (!user.getPassword().equals(loginRequest.getPassword())) {
                logger.warn("Login failed - incorrect password - username: {}", loginRequest.getUsername());
                return ResponseEntity.badRequest().body("密码错误");
            }

            // 登录成功，返回用户信息
            LoginResponse response = new LoginResponse(
                user.getId(),
                user.getUsername()
            );
            
            logger.info("Login successful - userId: {}, username: {}", user.getId(), user.getUsername());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Login error - username: {}, error: {}", loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.internalServerError().body("登录过程中发生错误");
        }
    }
} 