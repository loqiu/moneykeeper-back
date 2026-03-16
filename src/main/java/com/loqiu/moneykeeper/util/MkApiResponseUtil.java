package com.loqiu.moneykeeper.util;

import com.loqiu.moneykeeper.constant.ErrorKeyConstants;
import com.loqiu.moneykeeper.exception.PaymentException;
import com.loqiu.moneykeeper.response.MkApiResponse;

import java.util.Map;

public class MkApiResponseUtil {
    private static final Integer SUCCESS_CODE = 200;
    private static final Integer ERROR_CODE = 500;
    private static final String SUCCESS_MESSAGE = "Operation succeeded";

    public static <T> MkApiResponse<T> success(T data) {
        return MkApiResponse.<T>builder()
                .code(SUCCESS_CODE)
                .message(SUCCESS_MESSAGE)
                .data(data)
                .build();
    }

    public static <T> MkApiResponse<T> success(String message, T data) {
        return MkApiResponse.<T>builder()
                .code(SUCCESS_CODE)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> MkApiResponse<T> success() {
        return success(null);
    }

    public static <T> MkApiResponse<T> error(String message) {
        return MkApiResponse.error(ERROR_CODE, ErrorKeyConstants.COMMON_INTERNAL_SERVER_ERROR, Map.of(), message);
    }

    public static <T> MkApiResponse<T> error(int code, String message) {
        return MkApiResponse.error(code, message);
    }

    public static <T> MkApiResponse<T> error(int code, String errorKey, String message) {
        return MkApiResponse.error(code, errorKey, Map.of(), message);
    }

    public static <T> MkApiResponse<T> error(int code, String errorKey, Map<String, Object> errorParams, String message) {
        return MkApiResponse.error(code, errorKey, errorParams, message);
    }

    public static <T> MkApiResponse<T> error(PaymentException e) {
        return MkApiResponse.error(e.getCode(), "payment.processing_failed", Map.of(), e.getMessage());
    }

    public static <T> MkApiResponse<T> error(Exception e) {
        return MkApiResponse.error(ERROR_CODE, ErrorKeyConstants.COMMON_INTERNAL_SERVER_ERROR, Map.of(), e.getMessage());
    }

    public static boolean isSuccess(MkApiResponse<?> response) {
        return response != null && response.getCode() == SUCCESS_CODE;
    }

    public static <T> MkApiResponse<T> paymentError(String operation, String message) {
        return error("Payment " + operation + " failed: " + message);
    }

    public static <T> MkApiResponse<T> paymentSuccess(String operation, T data) {
        return success("Payment " + operation + " succeeded", data);
    }
}
