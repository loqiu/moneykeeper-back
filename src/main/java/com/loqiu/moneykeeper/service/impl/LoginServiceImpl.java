package com.loqiu.moneykeeper.service.impl;

import com.alibaba.fastjson.JSON;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

@Service
public class LoginServiceImpl implements LoginService {
    private static final Logger logger = LogManager.getLogger(LoginServiceImpl.class);

    @Value("${google.oauth2.clientId}")
    private String CLIENT_ID;

    @Value("${google.oauth2.ios.clientId}")
    private String IOS_CLIENT_ID;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private JwtUtil jwtUtil;
    @Override
    public LoginResponse verifyGoogleIdToken(String idTokenString){
        logger.info("Verifying Google ID token - Input - idTokenString: {}", idTokenString);
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
                    // Specify the CLIENT_ID of the app that accesses the backend:
                    .setAudience(Arrays.asList(CLIENT_ID,IOS_CLIENT_ID))
                    // Or, if multiple clients access the backend:
                    //.setAudience(Arrays.asList(CLIENT_ID_1, CLIENT_ID_2, CLIENT_ID_3))
                    .build();

            // (Receive idTokenString by HTTPS POST)
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                logger.info("ID token verified - Output - payload: {}", JSON.toJSONString(payload));

                // Get profile information from payload
                String userId = payload.getSubject();
                String email = payload.getEmail();
                boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
                String name = (String) payload.get("name");
                String pictureUrl = (String) payload.get("picture");
                String familyName = (String) payload.get("family_name");
                String givenName = (String) payload.get("given_name");

                // Use or store profile information
                // 生成JWT token
                String token = null;
                User user = userService.findByEmail(email);
                if(user == null){
                    // 创建新用户
                    User newUser = new User();
                    newUser.setUserPin(UserPinUtil.generateUserPin());
                    newUser.setUsername(name);
                    newUser.setPassword(passwordService.encodePassword(userId));
                    newUser.setEmail(email);
                    newUser.setFirstName(givenName);
                    newUser.setLastName(familyName);
                    newUser.setRegistrationCompletedAt(LocalDateTime.now());
                    // 保存用户
                    Boolean savedUser = userService.save(newUser);
                    token = jwtUtil.generateToken(newUser.getUserPin(), name);
                    logger.info("User registered successfully - result:{} userPin: {}, username: {}",savedUser, newUser.getUserPin(), newUser.getUsername());
                } else {
                    token = jwtUtil.generateToken(user.getUserPin(), name);
                    logger.info("User already exists login process");
                }
                // 返回登录信息
                LoginResponse response = new LoginResponse(
                        user.getId(),
                        user.getUserPin(),
                        name,
                        token
                );
                logger.info("verifyGoogleIdToken Login successful - response: {}", JSON.toJSONString(response));
                return response;
            } else {
                logger.info("Invalid ID token. token:{}", idTokenString);
                return null;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify ID token.", e);
        }
    }

}
