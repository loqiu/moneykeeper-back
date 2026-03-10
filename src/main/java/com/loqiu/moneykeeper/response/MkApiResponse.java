package com.loqiu.moneykeeper.response;

import java.time.LocalDateTime;

/**
 * Unified API response model.
 *
 * @param <T> response payload type
 */
public class MkApiResponse<T> {

    private Integer code;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    private String requestId;

    public MkApiResponse() {
        this.timestamp = LocalDateTime.now();
        this.requestId = generateRequestId();
    }

    private MkApiResponse(Builder<T> builder) {
        this.timestamp = LocalDateTime.now();
        this.requestId = generateRequestId();
        this.code = builder.code;
        this.message = builder.message;
        this.data = builder.data;
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static class Builder<T> {
        private Integer code;
        private String message;
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
        return MkApiResponse.<T>builder()
                .code(code)
                .message(message)
                .build();
    }

    public static <T> MkApiResponse<T> error(String message) {
        return MkApiResponse.<T>builder()
                .code(500)
                .message(message)
                .build();
    }

    @Override
    public String toString() {
        return "MkApiResponse{" +
                "code=" + code +
                ", message='" + message + '\'' +
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
}