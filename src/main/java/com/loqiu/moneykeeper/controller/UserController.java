package com.loqiu.moneykeeper.controller;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.loqiu.moneykeeper.entity.User;
import com.loqiu.moneykeeper.exception.BadRequestException;
import com.loqiu.moneykeeper.exception.ForbiddenException;
import com.loqiu.moneykeeper.exception.ResourceNotFoundException;
import com.loqiu.moneykeeper.service.PasswordService;
import com.loqiu.moneykeeper.service.UserService;
import com.loqiu.moneykeeper.util.RequestAuthUtil;
import com.loqiu.moneykeeper.util.UserPinUtil;
import com.loqiu.moneykeeper.vo.UserCreateRequest;
import com.loqiu.moneykeeper.vo.UserUpdateRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LogManager.getLogger(UserController.class);
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";
    private static final String PHONE_REGEX = "^\\d{10,11}$";

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordService passwordService;

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody UserCreateRequest createRequest, HttpServletRequest request) {
        Long currentUserId = RequestAuthUtil.requireCurrentUserId(request);
        logger.info("Creating user - requested by userId: {}", currentUserId);
        requireAdmin(request);
        validateCreateRequest(createRequest);

        if (userService.findByUsername(createRequest.getUsername()) != null) {
            throw new BadRequestException("Username already exists");
        }
        if (StringUtils.hasText(createRequest.getEmail()) && userService.findByEmail(createRequest.getEmail()) != null) {
            throw new BadRequestException("Email already exists");
        }

        User user = new User();
        user.setUserPin(UserPinUtil.generateUserPin());
        user.setUsername(createRequest.getUsername().trim());
        user.setPassword(passwordService.encodePassword(createRequest.getPassword()));
        user.setEmail(trimToNull(createRequest.getEmail()));
        user.setFirstName(trimToNull(createRequest.getFirstName()));
        user.setLastName(trimToNull(createRequest.getLastName()));
        user.setPhoneNumber(trimToNull(createRequest.getPhoneNumber()));
        user.setRole(resolveRole(createRequest.getRole()));
        user.setRegistrationCompletedAt(LocalDateTime.now());

        userService.save(user);
        logger.info("User created successfully - userId: {}, username: {}", user.getId(), user.getUsername());
        return ResponseEntity.ok(userService.getById(user.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id, HttpServletRequest request) {
        logger.info("Getting user - targetUserId: {}, currentUserId: {}", id, RequestAuthUtil.getCurrentUserId(request));
        requireSelfOrAdmin(request, id);
        return ResponseEntity.ok(requireUser(id));
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username, HttpServletRequest request) {
        logger.info("Getting user by username - username: {}, currentUserId: {}", username, RequestAuthUtil.getCurrentUserId(request));
        if (!StringUtils.hasText(username)) {
            throw new BadRequestException("Username cannot be empty");
        }
        if (!RequestAuthUtil.isAdmin(request) && !username.equals(RequestAuthUtil.requireCurrentUsername(request))) {
            throw new ForbiddenException("You can only access your own user profile");
        }

        User user = userService.findByUsername(username);
        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id,
                                           @RequestBody UserUpdateRequest updateRequest,
                                           HttpServletRequest request) {
        logger.info("Updating user - targetUserId: {}, currentUserId: {}", id, RequestAuthUtil.getCurrentUserId(request));
        requireSelfOrAdmin(request, id);
        validateUpdateRequest(updateRequest, request);

        User existingUser = requireUser(id);
        if (StringUtils.hasText(updateRequest.getUsername()) && !existingUser.getUsername().equals(updateRequest.getUsername().trim())) {
            User duplicatedUser = userService.findByUsername(updateRequest.getUsername().trim());
            if (duplicatedUser != null && !duplicatedUser.getId().equals(existingUser.getId())) {
                throw new BadRequestException("Username already exists");
            }
        }
        if (StringUtils.hasText(updateRequest.getEmail()) && !updateRequest.getEmail().trim().equals(existingUser.getEmail())) {
            User duplicatedEmailUser = userService.findByEmail(updateRequest.getEmail().trim());
            if (duplicatedEmailUser != null && !duplicatedEmailUser.getId().equals(existingUser.getId())) {
                throw new BadRequestException("Email already exists");
            }
        }

        User updatedUser = new User();
        updatedUser.setId(existingUser.getId());
        updatedUser.setUserPin(existingUser.getUserPin());
        updatedUser.setUsername(resolveString(updateRequest.getUsername(), existingUser.getUsername()));
        updatedUser.setPassword(resolvePassword(updateRequest.getPassword(), existingUser.getPassword()));
        updatedUser.setEmail(resolveNullableString(updateRequest.getEmail(), existingUser.getEmail()));
        updatedUser.setFirstName(resolveNullableString(updateRequest.getFirstName(), existingUser.getFirstName()));
        updatedUser.setLastName(resolveNullableString(updateRequest.getLastName(), existingUser.getLastName()));
        updatedUser.setPhoneNumber(resolveNullableString(updateRequest.getPhoneNumber(), existingUser.getPhoneNumber()));
        updatedUser.setRole(resolveUpdatedRole(updateRequest.getRole(), existingUser.getRole(), request));
        updatedUser.setRegistrationCompletedAt(existingUser.getRegistrationCompletedAt());
        updatedUser.setCreatedAt(existingUser.getCreatedAt());
        updatedUser.setDeletedAt(existingUser.getDeletedAt());
        updatedUser.setDeletedTime(existingUser.getDeletedTime());

        userService.updateById(updatedUser);
        logger.info("User updated successfully - userId: {}", id);
        return ResponseEntity.ok(requireUser(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id, HttpServletRequest request) {
        logger.info("Deleting user - targetUserId: {}, currentUserId: {}", id, RequestAuthUtil.getCurrentUserId(request));
        requireSelfOrAdmin(request, id);
        requireUser(id);

        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id)
                .set("deleted_at", 1)
                .set("deleted_time", LocalDateTime.now());
        userService.update(updateWrapper);
        logger.info("User deleted successfully - userId: {}", id);
        return ResponseEntity.ok().build();
    }

    private void validateCreateRequest(UserCreateRequest createRequest) {
        if (createRequest == null) {
            throw new BadRequestException("Request body is required");
        }
        validateRequiredText(createRequest.getUsername(), "Username is required");
        validateRequiredText(createRequest.getPassword(), "Password is required");
        validateUsername(createRequest.getUsername());
        validatePassword(createRequest.getPassword());
        validateOptionalEmail(createRequest.getEmail());
        validateOptionalPhone(createRequest.getPhoneNumber());
        validateOptionalRole(createRequest.getRole());
    }

    private void validateUpdateRequest(UserUpdateRequest updateRequest, HttpServletRequest request) {
        if (updateRequest == null) {
            throw new BadRequestException("Request body is required");
        }
        if (updateRequest.getUsername() != null) {
            validateRequiredText(updateRequest.getUsername(), "Username cannot be blank");
            validateUsername(updateRequest.getUsername());
        }
        if (updateRequest.getPassword() != null) {
            validateRequiredText(updateRequest.getPassword(), "Password cannot be blank");
            validatePassword(updateRequest.getPassword());
        }
        if (updateRequest.getEmail() != null) {
            validateOptionalEmail(updateRequest.getEmail());
        }
        if (updateRequest.getPhoneNumber() != null) {
            validateOptionalPhone(updateRequest.getPhoneNumber());
        }
        if (updateRequest.getRole() != null) {
            validateOptionalRole(updateRequest.getRole());
            if (!RequestAuthUtil.isAdmin(request)) {
                throw new ForbiddenException("Only admin users can change roles");
            }
        }
    }

    private void validateUsername(String username) {
        String trimmed = username.trim();
        if (trimmed.length() < 3 || trimmed.length() > 50) {
            throw new BadRequestException("Username length must be between 3 and 50 characters");
        }
    }

    private void validatePassword(String password) {
        if (password.length() < 6 || password.length() > 255) {
            throw new BadRequestException("Password length must be between 6 and 255 characters");
        }
    }

    private void validateOptionalEmail(String email) {
        if (email == null) {
            return;
        }
        if (!StringUtils.hasText(email)) {
            throw new BadRequestException("Email cannot be blank");
        }
        if (!Pattern.matches(EMAIL_REGEX, email.trim())) {
            throw new BadRequestException("Email format is invalid");
        }
    }

    private void validateOptionalPhone(String phoneNumber) {
        if (phoneNumber == null) {
            return;
        }
        if (!StringUtils.hasText(phoneNumber)) {
            throw new BadRequestException("Phone number cannot be blank");
        }
        if (!Pattern.matches(PHONE_REGEX, phoneNumber.trim())) {
            throw new BadRequestException("Phone number format is invalid");
        }
    }

    private void validateOptionalRole(String role) {
        if (role == null) {
            return;
        }
        if (!StringUtils.hasText(role)) {
            throw new BadRequestException("Role cannot be blank");
        }
        String normalizedRole = role.trim().toLowerCase();
        if (!"user".equals(normalizedRole) && !"admin".equals(normalizedRole)) {
            throw new BadRequestException("Role must be either user or admin");
        }
    }

    private void validateRequiredText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new BadRequestException(message);
        }
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
            throw new ForbiddenException("You do not have permission to access this user");
        }
    }

    private User requireUser(Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }
        return user;
    }

    private String resolvePassword(String incomingPassword, String existingPassword) {
        if (!StringUtils.hasText(incomingPassword)) {
            return existingPassword;
        }
        return passwordService.encodePassword(incomingPassword.trim());
    }

    private String resolveRole(String requestedRole) {
        if (!StringUtils.hasText(requestedRole)) {
            return "user";
        }
        return requestedRole.trim().toLowerCase();
    }

    private String resolveUpdatedRole(String requestedRole, String existingRole, HttpServletRequest request) {
        if (!StringUtils.hasText(requestedRole)) {
            return existingRole;
        }
        if (!RequestAuthUtil.isAdmin(request)) {
            throw new ForbiddenException("Only admin users can change roles");
        }
        return requestedRole.trim().toLowerCase();
    }

    private String resolveString(String requestedValue, String existingValue) {
        if (!StringUtils.hasText(requestedValue)) {
            return existingValue;
        }
        return requestedValue.trim();
    }

    private String resolveNullableString(String requestedValue, String existingValue) {
        if (requestedValue == null) {
            return existingValue;
        }
        return requestedValue.trim();
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}