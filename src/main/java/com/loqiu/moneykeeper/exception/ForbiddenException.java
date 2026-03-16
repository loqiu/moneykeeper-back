package com.loqiu.moneykeeper.exception;

import java.util.Map;

public class ForbiddenException extends ApiBusinessException {

    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(String message, String errorKey) {
        super(message, errorKey);
    }

    public ForbiddenException(String message, String errorKey, Map<String, Object> errorParams) {
        super(message, errorKey, errorParams);
    }
}
