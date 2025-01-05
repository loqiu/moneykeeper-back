package com.loqiu.moneykeeper.util;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class UserPinUtil {
    // 定义字符集：数字 + 大写字母 + 小写字母
    private static final String CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int PIN_LENGTH = 10; // PIN 长度
    private static final SecureRandom RANDOM = new SecureRandom();


    public static String generateUserPin(Long userId) {
        // 获取当前时间戳
        long timestamp = System.currentTimeMillis();

        // 组合 user_id 和时间戳
        String input = userId + "-" + timestamp;

        // 使用 Base64 编码
        String base64Encoded = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(input.getBytes(StandardCharsets.UTF_8));

        return base64Encoded;
    }

    public static String generateUserPin() {
        StringBuilder pin = new StringBuilder(PIN_LENGTH);
        for (int i = 0; i < PIN_LENGTH; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            pin.append(CHARACTERS.charAt(index));
        }
        return pin.toString();
    }
}
