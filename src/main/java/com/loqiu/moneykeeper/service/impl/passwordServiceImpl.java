package com.loqiu.moneykeeper.service.impl;

import com.loqiu.moneykeeper.service.PasswordService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class passwordServiceImpl implements PasswordService {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String encodePassword(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String rawPassword = "admin";
        String encodedPassword = encoder.encode(rawPassword);

        System.out.println("Encoded password: " + encodedPassword);

        System.out.println("Matches: " + encoder.matches(rawPassword, encodedPassword));
    }
}
