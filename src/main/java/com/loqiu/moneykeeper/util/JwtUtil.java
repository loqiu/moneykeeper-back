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
    private RedisTokenUtil redisTokenUtil;

    @Value("${JWT.SECERT}")
    private String secret;
    
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000;

    // 生成token
    public String generateToken(String userPin, String username) {
        String token = JWT.create()
                .withSubject(String.valueOf(userPin))
                .withClaim("username", username)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(Algorithm.HMAC256(secret));
        
        // 保存到Redis
        redisTokenUtil.saveToken(String.valueOf(userPin), token);
        return token;
    }

    // 验证token时同时检查Redis
    public DecodedJWT verifyToken(String token) throws JWTVerificationException {
        DecodedJWT jwt = JWT.require(Algorithm.HMAC256(secret))
                .build()
                .verify(token);
        
        // 验证Redis中的token
        String userPin = jwt.getSubject();
        if (!redisTokenUtil.validateToken(userPin, token)) {
            throw new JWTVerificationException("Token不存在或已失效");
        }
        
        return jwt;
    }

    // get userPin from token
    public String getUserIdFromToken(String token) {
        DecodedJWT jwt = verifyToken(token);
        return jwt.getSubject();
    }

    // 使token失效
    public void invalidateToken(String userPin) {
        redisTokenUtil.deleteToken(userPin);
    }
}