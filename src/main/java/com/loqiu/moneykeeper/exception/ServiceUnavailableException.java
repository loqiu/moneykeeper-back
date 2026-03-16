package com.loqiu.moneykeeper.exception;

import java.util.Map;

public class ServiceUnavailableException extends ApiBusinessException {

    public ServiceUnavailableException(String message) {
        super(message);
    }

    public ServiceUnavailableException(String message, String errorKey) {
        super(message, errorKey);
    }

    public ServiceUnavailableException(String message, String errorKey, Map<String, Object> errorParams) {
        super(message, errorKey, errorParams);
    }
}
