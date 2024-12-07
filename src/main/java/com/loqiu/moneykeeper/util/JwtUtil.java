package com.loqiu.moneykeeper.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {
    @Autowired
    private RedisUtil redisUtil;

    @Value("${JWT.SECERT}")
    private String secret;
    
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000;

    // 生成token
    public String generateToken(Long userId, String username) {
        String token = JWT.create()
                .withSubject(String.valueOf(userId))
                .withClaim("username", username)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(Algorithm.HMAC256(secret));
        
        // 保存到Redis
        redisUtil.saveToken(userId, token);
        return token;
    }

    // 验证token时同时检查Redis
    public DecodedJWT verifyToken(String token) throws JWTVerificationException {
        DecodedJWT jwt = JWT.require(Algorithm.HMAC256(secret))
                .build()
                .verify(token);
        
        // 验证Redis中的token
        Long userId = Long.parseLong(jwt.getSubject());
        if (!redisUtil.validateToken(userId, token)) {
            throw new JWTVerificationException("Token不存在或已失效");
        }
        
        return jwt;
    }

    // 从token中获取用户ID
    public Long getUserIdFromToken(String token) {
        DecodedJWT jwt = verifyToken(token);
        return Long.parseLong(jwt.getSubject());
    }

    // 使token失效
    public void invalidateToken(Long userId) {
        redisUtil.deleteToken(userId);
    }
} 