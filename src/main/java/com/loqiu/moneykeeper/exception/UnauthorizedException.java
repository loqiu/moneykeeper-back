package com.loqiu.moneykeeper.exception;

import java.util.Map;

public class UnauthorizedException extends ApiBusinessException {

    public UnauthorizedException() {
        super("Unauthorized");
    }

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, String errorKey) {
        super(message, errorKey);
    }

    public UnauthorizedException(String message, String errorKey, Map<String, Object> errorParams) {
        super(message, errorKey, errorParams);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause, null, null);
    }
}
