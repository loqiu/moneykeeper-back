package com.loqiu.moneykeeper.controller;

import com.loqiu.moneykeeper.constant.ErrorKeyConstants;
import com.loqiu.moneykeeper.entity.User;
import com.loqiu.moneykeeper.response.MkApiResponse;
import com.loqiu.moneykeeper.service.LoginService;
import com.loqiu.moneykeeper.service.PasswordService;
import com.loqiu.moneykeeper.service.UserService;
import com.loqiu.moneykeeper.util.JwtUtil;
import com.loqiu.moneykeeper.util.UserPinUtil;
import com.loqiu.moneykeeper.vo.GoogleAuthRequest;
import com.loqiu.moneykeeper.vo.LoginRequest;
import com.loqiu.moneykeeper.vo.LoginResponse;
import com.loqiu.moneykeeper.vo.RegisterRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/auth")
public class LoginController {

    private static final Logger logger = LogManager.getLogger(LoginController.class);
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";
    private static final String PHONE_REGEX = "^\\d{10,11}$";

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private LoginService loginService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public MkApiResponse<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        String username = loginRequest == null ? null : trimToNull(loginRequest.getUsername());
        logger.info("Processing login request - username: {}", username);
        if (!StringUtils.hasText(username) || loginRequest == null || !StringUtils.hasText(loginRequest.getPassword())) {
            logger.warn("Login rejected because username or password is missing");
            return MkApiResponse.error(400, ErrorKeyConstants.COMMON_BAD_REQUEST, Map.of(), "Username and password are required");
        }

        try {
            User user = userService.findByUsername(username);
            if (user == null) {
                logger.warn("Login failed - user not found - username: {}", username);
                return MkApiResponse.error(404, ErrorKeyConstants.AUTH_USER_NOT_FOUND, Map.of("username", username), "User not found");
            }

            if (!passwordService.matches(loginRequest.getPassword(), user.getPassword())) {
                logger.warn("Login failed - incorrect password - username: {}", username);
                return MkApiResponse.error(401, ErrorKeyConstants.AUTH_INVALID_CREDENTIALS, Map.of("username", username), "Incorrect password");
            }

            String token = jwtUtil.generateToken(user.getId(), user.getUserPin(), user.getUsername(), user.getRole());
            LoginResponse response = new LoginResponse(user.getId(), user.getUserPin(), user.getUsername(), token);
            logger.info("Login successful - userId: {}, username: {}", user.getId(), username);
            return MkApiResponse.success(response);
        } catch (Exception e) {
            logger.error("Login error - username: {}, error: {}", username, e.getMessage());
            return MkApiResponse.error(500, ErrorKeyConstants.COMMON_INTERNAL_SERVER_ERROR, Map.of(), "Login failed");
        }
    }

    @PostMapping("/logout")
    public MkApiResponse<Boolean> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        if (!StringUtils.hasText(token) || !token.startsWith("Bearer ")) {
            logger.warn("Logout failed - invalid token header");
            return MkApiResponse.error(401, ErrorKeyConstants.AUTH_INVALID_AUTH_HEADER, Map.of(), "Invalid Authorization header");
        }
        try {
            String userPin = jwtUtil.getUserPinFromToken(token.substring(7));
            jwtUtil.invalidateToken(userPin);
            logger.info("Logout successful - userPin: {}", userPin);
            return MkApiResponse.success("Logout successful", Boolean.TRUE);
        } catch (Exception e) {
            logger.error("Logout error - error: {}", e.getMessage());
            return MkApiResponse.error(401, ErrorKeyConstants.AUTH_INVALID_TOKEN, Map.of(), "Invalid token");
        }
    }

    @PostMapping("/register")
    public MkApiResponse<User> register(@RequestBody RegisterRequest registerRequest) {
        logger.info("Processing register request - username: {}, email: {}",
                registerRequest == null ? null : registerRequest.getUsername(),
                registerRequest == null ? null : registerRequest.getEmail());

        try {
            String validationError = validateRegisterRequest(registerRequest);
            if (validationError != null) {
                logger.warn("Register validation failed - {}", validationError);
                return MkApiResponse.error(400, ErrorKeyConstants.COMMON_BAD_REQUEST, Map.of(), validationError);
            }

            String username = registerRequest.getUsername().trim();
            String email = registerRequest.getEmail().trim();
            if (userService.findByUsername(username) != null) {
                logger.warn("Register failed - username already exists - username: {}", username);
                return MkApiResponse.error(409, ErrorKeyConstants.AUTH_USERNAME_EXISTS, Map.of("username", username), "Username already exists");
            }

            if (userService.findByEmail(email) != null) {
                logger.warn("Register failed - email already exists - email: {}", email);
                return MkApiResponse.error(409, ErrorKeyConstants.AUTH_EMAIL_EXISTS, Map.of("email", email), "Email already exists");
            }

            User newUser = new User();
            newUser.setUserPin(UserPinUtil.generateUserPin());
            newUser.setUsername(username);
            newUser.setPassword(passwordService.encodePassword(registerRequest.getPassword()));
            newUser.setEmail(email);
            newUser.setFirstName(registerRequest.getFirstName().trim());
            newUser.setLastName(registerRequest.getLastName().trim());
            newUser.setPhoneNumber(trimToNull(registerRequest.getPhoneNumber()));
            newUser.setRole("user");
            newUser.setRegistrationCompletedAt(LocalDateTime.now());

            boolean savedUser = userService.save(newUser);
            if (!savedUser) {
                logger.error("Register failed during save - username: {}", username);
                return MkApiResponse.error(500, ErrorKeyConstants.COMMON_INTERNAL_SERVER_ERROR, Map.of(), "Register failed");
            }
            logger.info("Register successful - userId: {}, username: {}", newUser.getId(), newUser.getUsername());
            return MkApiResponse.success(newUser);
        } catch (Exception e) {
            logger.error("Register error - username: {}, error: {}",
                    registerRequest == null ? null : registerRequest.getUsername(), e.getMessage());
            return MkApiResponse.error(500, ErrorKeyConstants.COMMON_INTERNAL_SERVER_ERROR, Map.of(), "Register failed");
        }
    }

    @PostMapping("/google")
    public MkApiResponse<LoginResponse> googleAuth(@RequestBody GoogleAuthRequest request) {
        String idToken = request == null ? null : trimToNull(request.getIdToken());
        logger.info("Processing Google login request");
        if (!StringUtils.hasText(idToken)) {
            logger.warn("Google login failed - empty idToken");
            return MkApiResponse.error(400, ErrorKeyConstants.AUTH_GOOGLE_ID_TOKEN_REQUIRED, Map.of(), "Google idToken is required");
        }
        try {
            LoginResponse response = loginService.verifyGoogleIdToken(idToken);
            if (response == null) {
                return MkApiResponse.error(401, ErrorKeyConstants.AUTH_GOOGLE_LOGIN_FAILED, Map.of(), "Google login failed");
            }
            return MkApiResponse.success(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Google login rejected - reason: {}", e.getMessage());
            return MkApiResponse.error(401, ErrorKeyConstants.AUTH_GOOGLE_LOGIN_FAILED, Map.of(), e.getMessage());
        } catch (Exception e) {
            logger.error("Google login error - error: {}", e.getMessage());
            return MkApiResponse.error(500, ErrorKeyConstants.COMMON_INTERNAL_SERVER_ERROR, Map.of(), "Google login failed");
        }
    }

    private String validateRegisterRequest(RegisterRequest request) {
        if (request == null) {
            return "Register request cannot be empty";
        }
        String username = trimToNull(request.getUsername());
        if (!StringUtils.hasText(username)) {
            return "Username cannot be empty";
        }
        if (username.length() < 3 || username.length() > 50) {
            return "Username length must be between 3 and 50 characters";
        }
        if (!StringUtils.hasText(request.getPassword())) {
            return "Password cannot be empty";
        }
        if (request.getPassword().length() < 6 || request.getPassword().length() > 255) {
            return "Password length must be between 6 and 255 characters";
        }
        String email = trimToNull(request.getEmail());
        if (!StringUtils.hasText(email)) {
            return "Email cannot be empty";
        }
        if (!Pattern.matches(EMAIL_REGEX, email)) {
            return "Email format is invalid";
        }
        String firstName = trimToNull(request.getFirstName());
        if (!StringUtils.hasText(firstName)) {
            return "First name cannot be empty";
        }
        if (firstName.length() > 50) {
            return "First name length cannot exceed 50 characters";
        }
        String lastName = trimToNull(request.getLastName());
        if (!StringUtils.hasText(lastName)) {
            return "Last name cannot be empty";
        }
        if (lastName.length() > 50) {
            return "Last name length cannot exceed 50 characters";
        }
        String phoneNumber = trimToNull(request.getPhoneNumber());
        if (phoneNumber != null && !Pattern.matches(PHONE_REGEX, phoneNumber)) {
            return "Phone number format is invalid";
        }
        return null;
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
