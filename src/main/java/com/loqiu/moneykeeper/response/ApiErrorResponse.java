package com.loqiu.moneykeeper.response;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiErrorResponse(
        int status,
        String error,
        String errorKey,
        Map<String, Object> errorParams,
        String message,
        String path,
        LocalDateTime timestamp,
        String traceId
) {
}
