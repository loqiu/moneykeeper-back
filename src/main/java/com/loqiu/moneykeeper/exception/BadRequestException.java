package com.loqiu.moneykeeper.exception;

import java.util.Map;

public class BadRequestException extends ApiBusinessException {

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, String errorKey) {
        super(message, errorKey);
    }

    public BadRequestException(String message, String errorKey, Map<String, Object> errorParams) {
        super(message, errorKey, errorParams);
    }
}
