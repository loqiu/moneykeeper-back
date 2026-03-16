package com.loqiu.moneykeeper.exception;

import java.util.Map;

public class ResourceNotFoundException extends ApiBusinessException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, String errorKey) {
        super(message, errorKey);
    }

    public ResourceNotFoundException(String message, String errorKey, Map<String, Object> errorParams) {
        super(message, errorKey, errorParams);
    }
}
