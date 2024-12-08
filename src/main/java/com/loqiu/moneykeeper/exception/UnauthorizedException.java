package com.loqiu.moneykeeper.exception;

public class UnauthorizedException extends RuntimeException {
    
    public UnauthorizedException() {
        super("未授权的访问");
    }

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
} 