package com.loqiu.moneykeeper.exception;

import java.util.Map;

public class ConflictException extends ApiBusinessException {

    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String message, String errorKey) {
        super(message, errorKey);
    }

    public ConflictException(String message, String errorKey, Map<String, Object> errorParams) {
        super(message, errorKey, errorParams);
    }
}
