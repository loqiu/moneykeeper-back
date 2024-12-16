package com.loqiu.moneykeeper.response;

import java.time.LocalDateTime;

/**
 * 统一API响应类
 * @param <T> 响应数据类型
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
            // 参数验证
            if (code == null) {
                throw new IllegalStateException("code cannot be null");
            }
            if (message == null) {
                throw new IllegalStateException("message cannot be null");
            }
            return new MkApiResponse<>(this);
        }
    }

    // 便捷的静态工厂方法
    public static <T> MkApiResponse<T> success(T data) {
        return MkApiResponse.<T>builder()
                .code(200)
                .message("操作成功")
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

    // Getters and Setters
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