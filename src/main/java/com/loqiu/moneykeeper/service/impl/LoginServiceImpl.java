package com.loqiu.moneykeeper.service.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.loqiu.moneykeeper.entity.User;
import com.loqiu.moneykeeper.service.LoginService;
import com.loqiu.moneykeeper.service.PasswordService;
import com.loqiu.moneykeeper.service.UserService;
import com.loqiu.moneykeeper.util.JwtUtil;
import com.loqiu.moneykeeper.util.UserPinUtil;
import com.loqiu.moneykeeper.vo.LoginResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;

@Service
public class LoginServiceImpl implements LoginService {
    private static final Logger logger = LogManager.getLogger(LoginServiceImpl.class);

    @Value("${google.oauth2.clientId}")
    private String clientId;

    @Value("${google.oauth2.ios.clientId}")
    private String iosClientId;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public LoginResponse verifyGoogleIdToken(String idTokenString) {
        logger.info("Verifying Google ID token");
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
                    .setAudience(Arrays.asList(clientId, iosClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                logger.warn("Invalid Google ID token");
                throw new IllegalArgumentException("Invalid Google ID token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String googleUserId = payload.getSubject();
            String email = payload.getEmail();
            boolean emailVerified = Boolean.TRUE.equals(payload.getEmailVerified());
            String name = (String) payload.get("name");
            String familyName = (String) payload.get("family_name");
            String givenName = (String) payload.get("given_name");

            if (!emailVerified || !StringUtils.hasText(email)) {
                throw new IllegalArgumentException("Google account email is not verified");
            }

            User user = userService.findByEmail(email);
            if (user == null) {
                User newUser = new User();
                newUser.setUserPin(UserPinUtil.generateUserPin());
                newUser.setUsername(StringUtils.hasText(name) ? name : email);
                newUser.setPassword(passwordService.encodePassword(googleUserId));
                newUser.setEmail(email);
                newUser.setFirstName(givenName);
                newUser.setLastName(familyName);
                newUser.setRole("user");
                newUser.setRegistrationCompletedAt(LocalDateTime.now());
                boolean savedUser = userService.save(newUser);
                if (!savedUser) {
                    throw new IllegalStateException("Failed to create Google user");
                }
                logger.info("Created new Google user - userId: {}, email: {}", newUser.getId(), email);
                user = newUser;
            } else {
                logger.info("Google login for existing user - userId: {}, email: {}", user.getId(), email);
            }

            String token = jwtUtil.generateToken(user.getId(), user.getUserPin(), user.getUsername(), user.getRole());
            LoginResponse response = new LoginResponse(user.getId(), user.getUserPin(), user.getUsername(), token);
            logger.info("Google login successful - userId: {}", user.getId());
            return response;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify Google ID token", e);
        }
    }
}