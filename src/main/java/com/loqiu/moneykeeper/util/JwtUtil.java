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

    public String generateToken(Long userId, String userPin, String username, String role) {
        String resolvedRole = role == null || role.isBlank() ? "user" : role;
        String token = JWT.create()
                .withSubject(userPin)
                .withClaim("userId", userId)
                .withClaim("username", username)
                .withClaim("role", resolvedRole)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(Algorithm.HMAC256(secret));

        redisTokenUtil.saveToken(userPin, token);
        return token;
    }

    public DecodedJWT verifyToken(String token) throws JWTVerificationException {
        DecodedJWT jwt = JWT.require(Algorithm.HMAC256(secret))
                .build()
                .verify(token);

        String userPin = jwt.getSubject();
        if (!redisTokenUtil.validateToken(userPin, token)) {
            throw new JWTVerificationException("Token is invalid or expired");
        }

        return jwt;
    }

    public String getUserPinFromToken(String token) {
        DecodedJWT jwt = verifyToken(token);
        return jwt.getSubject();
    }

    // Legacy name kept to avoid widespread call-site changes.
    public String getUserIdFromToken(String token) {
        return getUserPinFromToken(token);
    }

    public void invalidateToken(String userPin) {
        redisTokenUtil.deleteToken(userPin);
    }
}
