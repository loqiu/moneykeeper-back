package com.loqiu.moneykeeper.enums;

import com.loqiu.moneykeeper.exception.PaymentException;
import lombok.Getter;

@Getter
public enum ErrorCode {
    // 通用错误码 (1000-1999)
    SUCCESS(200, "操作成功"),
    SYSTEM_ERROR(500, "系统错误"),
    PARAM_ERROR(1001, "参数错误"),
    UNAUTHORIZED(1002, "未授权访问"),
    FORBIDDEN(1003, "禁止访问"),
    NOT_FOUND(1004, "资源不存在"),
    DUPLICATE_ERROR(1005, "数据重复"),


    // 业务相关错误码 (3000-3999)
    BUSINESS_ERROR(3000, "业务处理失败"),
    INVALID_OPERATION(3001, "无效的操作"),
    DATA_NOT_FOUND(3002, "数据不存在"),
    DATA_ALREADY_EXISTS(3003, "数据已存在"),
    DATA_VALIDATION_FAILED(3004, "数据验证失败");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public static ErrorCode fromCode(int code) {
        for (ErrorCode errorCode : ErrorCode.values()) {
            if (errorCode.getCode() == code) {
                return errorCode;
            }
        }
        return SYSTEM_ERROR;
    }

    public static String getMessageByCode(int code) {
        for (ErrorCode errorCode : ErrorCode.values()) {
            if (errorCode.getCode() == code) {
                return errorCode.getMessage();
            }
        }
        return SYSTEM_ERROR.getMessage();
    }

    public PaymentException toException() {
        return new PaymentException(this.code, this.message);
    }

    public PaymentException toException(String detail) {
        return new PaymentException(this.code, this.message + (detail != null ? ": " + detail : ""));
    }

    public PaymentException toException(Throwable cause) {
        return new PaymentException(this.code, this.message, cause);
    }
} 