package com.loqiu.moneykeeper.response;

import com.loqiu.moneykeeper.common.TraceContext;
import com.loqiu.moneykeeper.constant.ErrorKeyConstants;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Unified API response model.
 *
 * @param <T> response payload type
 */
public class MkApiResponse<T> {

    private Integer code;
    private String message;
    private String errorKey;
    private Map<String, Object> errorParams;
    private T data;
    private LocalDateTime timestamp;
    private String requestId;
    private String traceId;

    public MkApiResponse() {
        this.timestamp = LocalDateTime.now();
        this.requestId = generateRequestId();
        this.traceId = TraceContext.getTraceId();
        this.errorParams = Map.of();
    }

    private MkApiResponse(Builder<T> builder) {
        this.timestamp = LocalDateTime.now();
        this.requestId = generateRequestId();
        this.traceId = TraceContext.getTraceId();
        this.code = builder.code;
        this.message = builder.message;
        this.errorKey = builder.errorKey;
        this.errorParams = builder.errorParams == null ? Map.of() : Map.copyOf(builder.errorParams);
        this.data = builder.data;
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static class Builder<T> {
        private Integer code;
        private String message;
        private String errorKey;
        private Map<String, Object> errorParams;
        private T data;

        private Builder() {
        }

        public Builder<T> code(Integer code) {
            this.code = code;
            return this;
        }

        public Builder<T> message(String message) {
            this.message = message;
            return this;
        }

        public Builder<T> errorKey(String errorKey) {
            this.errorKey = errorKey;
            return this;
        }

        public Builder<T> errorParams(Map<String, Object> errorParams) {
            this.errorParams = errorParams;
            return this;
        }

        public Builder<T> data(T data) {
            this.data = data;
            return this;
        }

        public MkApiResponse<T> build() {
            if (code == null) {
                throw new IllegalStateException("code cannot be null");
            }
            if (message == null) {
                throw new IllegalStateException("message cannot be null");
            }
            return new MkApiResponse<>(this);
        }
    }

    public static <T> MkApiResponse<T> success(T data) {
        return MkApiResponse.<T>builder()
                .code(200)
                .message("Operation succeeded")
                .data(data)
                .build();
    }

    public static <T> MkApiResponse<T> success(String message, T data) {
        return MkApiResponse.<T>builder()
                .code(200)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> MkApiResponse<T> error(Integer code, String message) {
        return error(code, ErrorKeyConstants.defaultForStatus(code == null ? 500 : code), Map.of(), message);
    }

    public static <T> MkApiResponse<T> error(Integer code, String errorKey, String message) {
        return error(code, errorKey, Map.of(), message);
    }

    public static <T> MkApiResponse<T> error(Integer code, String errorKey, Map<String, Object> errorParams, String message) {
        return MkApiResponse.<T>builder()
                .code(code)
                .errorKey(errorKey)
                .errorParams(errorParams)
                .message(message)
                .build();
    }

    public static <T> MkApiResponse<T> error(String message) {
        return error(500, ErrorKeyConstants.COMMON_INTERNAL_SERVER_ERROR, Map.of(), message);
    }

    @Override
    public String toString() {
        return "MkApiResponse{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", errorKey='" + errorKey + '\'' +
                ", data=" + data +
                '}';
    }

    private String generateRequestId() {
        return String.format("REQ-%d", System.currentTimeMillis());
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorKey() {
        return errorKey;
    }

    public void setErrorKey(String errorKey) {
        this.errorKey = errorKey;
    }

    public Map<String, Object> getErrorParams() {
        return errorParams;
    }

    public void setErrorParams(Map<String, Object> errorParams) {
        this.errorParams = errorParams;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getTraceId() {
        return traceId;
    }
}
