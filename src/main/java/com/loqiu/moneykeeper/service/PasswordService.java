package com.loqiu.moneykeeper.service;

public interface PasswordService {

    public String encodePassword(String rawPassword);
    public boolean matches(String rawPassword, String encodedPassword);
}
