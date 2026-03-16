package com.loqiu.moneykeeper.exception;

import java.util.Map;

public class ApiBusinessException extends RuntimeException {

    private final String errorKey;
    private final Map<String, Object> errorParams;

    public ApiBusinessException(String message) {
        this(message, null, null);
    }

    public ApiBusinessException(String message, String errorKey) {
        this(message, errorKey, null);
    }

    public ApiBusinessException(String message, String errorKey, Map<String, Object> errorParams) {
        super(message);
        this.errorKey = errorKey;
        this.errorParams = errorParams == null ? Map.of() : Map.copyOf(errorParams);
    }

    public ApiBusinessException(String message, Throwable cause, String errorKey, Map<String, Object> errorParams) {
        super(message, cause);
        this.errorKey = errorKey;
        this.errorParams = errorParams == null ? Map.of() : Map.copyOf(errorParams);
    }

    public String getErrorKey() {
        return errorKey;
    }

    public Map<String, Object> getErrorParams() {
        return errorParams;
    }
}
