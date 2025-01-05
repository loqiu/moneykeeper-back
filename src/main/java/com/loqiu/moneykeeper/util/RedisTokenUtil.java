package com.loqiu.moneykeeper.util;

import com.loqiu.moneykeeper.constant.RedisKeyConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisTokenUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final long TOKEN_EXPIRATION = 24; // 24小时

    // 存储token
    public void saveToken(String userPin, String token) {
        String key = RedisKeyConstant.TOKEN_PREFIX + userPin;
        redisTemplate.opsForValue().set(key, token, TOKEN_EXPIRATION, TimeUnit.HOURS);
    }

    // 获取token
    public String getToken(String userPin) {
        String key = RedisKeyConstant.TOKEN_PREFIX + userPin;
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? value.toString() : null;
    }

    // 删除token
    public void deleteToken(String userPin) {
        String key = RedisKeyConstant.TOKEN_PREFIX + userPin;
        redisTemplate.delete(key);
    }

    // 验证token是否存在
    public boolean validateToken(String userPin, String token) {
        String storedToken = getToken(userPin);
        return token != null && token.equals(storedToken);
    }
} 