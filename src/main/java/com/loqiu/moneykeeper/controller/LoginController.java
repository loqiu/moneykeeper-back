package com.loqiu.moneykeeper.controller;

import com.alibaba.fastjson2.JSON;
import com.loqiu.moneykeeper.entity.User;
import com.loqiu.moneykeeper.response.MkApiResponse;
import com.loqiu.moneykeeper.service.UserService;
import com.loqiu.moneykeeper.util.JwtUtil;
import com.loqiu.moneykeeper.vo.LoginRequest;
import com.loqiu.moneykeeper.vo.LoginResponse;
import com.loqiu.moneykeeper.vo.RegisterRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import com.loqiu.moneykeeper.service.PasswordService;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/auth")
public class LoginController {

    private static final Logger logger = LogManager.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordService passwordService;
    
    @Autowired
    private JwtUtil jwtUtil;

    // 邮箱格式正则表达式
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";
    // 手机号格式正则表达式
    private static final String   PHONE_REGEX = "^\\d{10,11}$";

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
            if (!passwordService.matches(loginRequest.getPassword(), user.getPassword())) {
                logger.warn("Login failed - incorrect password - username: {}", loginRequest.getUsername());
                return ResponseEntity.badRequest().body("密码错误");
            }

            // 生成JWT token
            String token = jwtUtil.generateToken(user.getId(), user.getUsername());

            // 登录成功，返回用户信息和token
            LoginResponse response = new LoginResponse(
                user.getId(),
                user.getUsername(),
                token
            );
            
            logger.info("Login successful - userId: {}, username: {}", user.getId(), user.getUsername());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Login error - username: {}, error: {}", loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.internalServerError().body("登录过程中发生错误");
        }
    }

    @PostMapping("/logout")
    public MkApiResponse<Boolean> logout(@RequestHeader("Authorization") String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                Long userId = jwtUtil.getUserIdFromToken(token);
                jwtUtil.invalidateToken(userId);
                logger.info("用户登出成功 - 用户ID: {}", userId);
                logger.info("result:{}", JSON.toJSONString(MkApiResponse.success("登出成功", Boolean.TRUE)));
                return MkApiResponse.success("登出成功", Boolean.TRUE);
            }
            logger.warn("登出失败 - 无效的token");
            return MkApiResponse.error("无效的token");
        } catch (Exception e) {
            logger.error("登出过程发生错误 - 错误: {}", e.getMessage());
            return MkApiResponse.error("登出失败");
        }
    }

    @PostMapping("/register")
    public MkApiResponse<User> register(@RequestBody RegisterRequest registerRequest) {
        logger.info("开始处理注册请求 - username: {}, email: {}", registerRequest.getUsername(), registerRequest.getEmail());
        
        try {
            // 参数校验
            String validationError = validateRegisterRequest(registerRequest);
            if (validationError != null) {
                logger.warn("注册请求参数验证失败 - {}", validationError);
                return MkApiResponse.error(validationError);
            }

            // 检查用户名是否已存在
            if (userService.findByUsername(registerRequest.getUsername()) != null) {
                logger.warn("注册失败 - 用户名已存在 - username: {}", registerRequest.getUsername());
                return MkApiResponse.error("用户名已存在");
            }

            // 检查邮箱是否已存在
            if (userService.findByEmail(registerRequest.getEmail()) != null) {
                logger.warn("注册失败 - 邮箱已存在 - email: {}", registerRequest.getEmail());
                return MkApiResponse.error("邮箱已被使用");
            }

            // 创建新用户
            User newUser = new User();
            newUser.setUsername(registerRequest.getUsername());
            newUser.setPassword(passwordService.encodePassword(registerRequest.getPassword()));
            newUser.setEmail(registerRequest.getEmail());
            newUser.setFirstName(registerRequest.getFirstName());
            newUser.setLastName(registerRequest.getLastName());
            newUser.setPhoneNumber(registerRequest.getPhoneNumber());
            newUser.setRegistrationCompletedAt(LocalDateTime.now());

            // 保存用户
            Boolean savedUser = userService.save(newUser);
            logger.info("用户注册成功 - result:{} userId: {}, username: {}",savedUser, newUser.getId(), newUser.getUsername());
            
            return MkApiResponse.success(newUser);
        } catch (Exception e) {
            logger.error("注册过程发生错误 - username: {}, error: {}", registerRequest.getUsername(), e.getMessage());
            return MkApiResponse.error("注册失败：" + e.getMessage());
        }
    }

    /**
     * 验证注册请求参数
     * @param request 注册请求
     * @return 如果验证通过返回null，否则返回错误信息
     */
    private String validateRegisterRequest(RegisterRequest request) {
        if (request == null) {
            return "注册信息不能为空";
        }
        
        if (!StringUtils.hasText(request.getUsername())) {
            return "用户名不能为空";
        }
        if (request.getUsername().length() < 3 || request.getUsername().length() > 50) {
            return "用户名长度必须在3-50个字符之间";
        }
        
        if (!StringUtils.hasText(request.getPassword())) {
            return "密码不能为空";
        }
        if (request.getPassword().length() < 6 || request.getPassword().length() > 255) {
            return "密码长度必须在6-255个字符之间";
        }
        
        if (!StringUtils.hasText(request.getEmail())) {
            return "邮箱不能为空";
        }
        if (!Pattern.matches(EMAIL_REGEX, request.getEmail())) {
            return "邮箱格式不正确";
        }
        
        if (!StringUtils.hasText(request.getFirstName())) {
            return "名字不能为空";
        }
        if (request.getFirstName().length() > 50) {
            return "名字长度不能超过50个字符";
        }
        
        if (!StringUtils.hasText(request.getLastName())) {
            return "姓氏不能为空";
        }
        if (request.getLastName().length() > 50) {
            return "姓氏长度不能超过50个字符";
        }
        
        if (StringUtils.hasText(request.getPhoneNumber()) && !Pattern.matches(PHONE_REGEX, request.getPhoneNumber())) {
            return "手机号格式不正确";
        }
        
        return null;
    }

} 