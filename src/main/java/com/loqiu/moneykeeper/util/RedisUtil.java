package com.loqiu.moneykeeper.util;

import com.loqiu.moneykeeper.constant.RedisKeyConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final long TOKEN_EXPIRATION = 24; // 24小时

    // 存储token
    public void saveToken(Long userId, String token) {
        String key = RedisKeyConstant.TOKEN_PREFIX + userId;
        redisTemplate.opsForValue().set(key, token, TOKEN_EXPIRATION, TimeUnit.HOURS);
    }

    // 获取token
    public String getToken(Long userId) {
        String key = RedisKeyConstant.TOKEN_PREFIX + userId;
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? value.toString() : null;
    }

    // 删除token
    public void deleteToken(Long userId) {
        String key = RedisKeyConstant.TOKEN_PREFIX + userId;
        redisTemplate.delete(key);
    }

    // 验证token是否存在
    public boolean validateToken(Long userId, String token) {
        String storedToken = getToken(userId);
        return token != null && token.equals(storedToken);
    }
} 