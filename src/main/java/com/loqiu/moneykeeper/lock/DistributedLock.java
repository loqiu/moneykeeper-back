package com.loqiu.moneykeeper.lock;


import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class DistributedLock {

    private final RedisTemplate<String, String> redisTemplate;
    private final String lockKey;
    private final ReentrantLock localLock;
    private static final long DEFAULT_EXPIRE_TIME = 10000; // 10秒

    public DistributedLock(RedisTemplate redisTemplate, String lockKey) {
        this.redisTemplate = redisTemplate;
        this.lockKey = lockKey;
        this.localLock = new ReentrantLock();
    }

    public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        long timeoutMillis = unit.toMillis(timeout);

        // 尝试获取本地锁
        if (!localLock.tryLock(timeoutMillis, TimeUnit.MILLISECONDS)) {
            return false;
        }

        try {
            while (System.currentTimeMillis() - startTime < timeoutMillis) {
                long expireAt = System.currentTimeMillis() + DEFAULT_EXPIRE_TIME;
                String expireAtStr = String.valueOf(expireAt);

                // 尝试通过Redis获取分布式锁
                if (redisTemplate.opsForValue().setIfAbsent(lockKey, expireAtStr, DEFAULT_EXPIRE_TIME,TimeUnit.MILLISECONDS).equals(Boolean.TRUE)) {
                    return true;
                }

                // 检查锁是否已过期
                String currentValue = redisTemplate.opsForValue().get(lockKey);
                if (currentValue != null && Long.parseLong(currentValue) < System.currentTimeMillis()) {
                    // 强制解锁：原子更新锁的值
                    String oldValue = redisTemplate.opsForValue().getAndSet(lockKey, expireAtStr);
                    if (oldValue != null && oldValue.equals(currentValue)) {
                        return true;
                    }
                }

                // 等待一会再尝试
                Thread.sleep(100);
            }

            return false;
        } finally {
            if (!localLock.isHeldByCurrentThread()) {
                localLock.unlock();
            }
        }
    }

    public void unlock() {
        try {
            String currentValue = redisTemplate.opsForValue().get(lockKey);
            if (currentValue != null && Long.parseLong(currentValue) > System.currentTimeMillis()) {
                redisTemplate.delete(lockKey); // 删除Redis锁
            }
        } finally {
            if (localLock.isHeldByCurrentThread()) {
                localLock.unlock(); // 释放本地锁
            }
        }
    }

    public static void main(String[] args) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        DistributedLock distributedLock = new DistributedLock(redisTemplate, "my_distributed_lock");

        try {
            if (distributedLock.tryLock(5, TimeUnit.SECONDS)) {
                System.out.println("获取分布式锁成功！");
                // 执行业务逻辑
                Thread.sleep(5000);
            } else {
                System.out.println("获取分布式锁失败！");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            distributedLock.unlock();
            System.out.println("释放分布式锁！");
        }
    }
}

