package com.loqiu.moneykeeper.controller;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.loqiu.moneykeeper.entity.User;
import com.loqiu.moneykeeper.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LogManager.getLogger(UserController.class);

    @Autowired
    private UserService userService;
    
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        logger.info("Creating user - Input - user: {}", user);
        
        if (user == null || user.getUsername() == null || user.getPassword() == null) {
            logger.error("Invalid input - user data is incomplete: {}", user);
            return ResponseEntity.badRequest().build();
        }
        
        try {
            // 检查用户名是否已存在
            User existingUser = userService.findByUsername(user.getUsername());
            if (existingUser != null) {
                logger.warn("Username already exists - username: {}", user.getUsername());
                return ResponseEntity.badRequest().build();
            }
            
            userService.save(user);
            logger.info("User created successfully - Output - user: {}", user);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            logger.error("Failed to create user - error: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        logger.info("Getting user - Input - userId: {}", id);
        
        if (id == null) {
            logger.error("Invalid input - userId is null");
            return ResponseEntity.badRequest().build();
        }
        
        try {
            User user = userService.getById(id);
            if (user != null) {
                logger.info("User found - Output - user: {}", user);
                return ResponseEntity.ok(user);
            } else {
                logger.warn("User not found - userId: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Failed to get user - userId: {}, error: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/username/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        logger.info("Getting user by username - Input - username: {}", username);
        
        if (username == null || username.trim().isEmpty()) {
            logger.error("Invalid input - username is null or empty");
            return ResponseEntity.badRequest().build();
        }
        
        try {
            User user = userService.findByUsername(username);
            if (user != null) {
                logger.info("User found - Output - user: {}", user);
                return ResponseEntity.ok(user);
            } else {
                logger.warn("User not found - username: {}", username);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Failed to get user - username: {}, error: {}", username, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        logger.info("Updating user - Input - userId: {}, user: {}", id, user);
        
        if (id == null || user == null) {
            logger.error("Invalid input - userId: {}, user: {}", id, user);
            return ResponseEntity.badRequest().build();
        }
        
        try {
            User existingUser = userService.getById(id);
            if (existingUser == null) {
                logger.warn("User not found - userId: {}", id);
                return ResponseEntity.notFound().build();
            }
            
            if (!existingUser.getId().equals(user.getId())) {
                logger.error("User id mismatch - pathId: {}, userId: {}", id, user.getId());
                return ResponseEntity.badRequest().build();
            }
            
            userService.updateById(user);
            logger.info("User updated successfully - Output - user: {}", user);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            logger.error("Failed to update user - userId: {}, error: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        logger.info("Deleting user - Input - userId: {}", id);
        
        if (id == null) {
            logger.error("Invalid input - userId is null");
            return ResponseEntity.badRequest().build();
        }
        
        try {
            User existingUser = userService.getById(id);
            if (existingUser == null) {
                logger.warn("User not found - userId: {}", id);
                return ResponseEntity.notFound().build();
            }
            LocalDateTime currentTime = LocalDateTime.now();
            UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", id)
                    .set("deleted_at", 1)
                    .set("deleted_time", currentTime);
            logger.info("User updateWrapper - userId: {}, updateWrapper: {}", id, updateWrapper);
            userService.update(updateWrapper);
            logger.info("User deleted successfully - userId: {}", id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Failed to delete user - userId: {}, error: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

} 