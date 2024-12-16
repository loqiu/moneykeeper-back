package com.loqiu.moneykeeper.exception;

import com.loqiu.moneykeeper.enums.PaymentErrorCode;

public class PaymentException extends RuntimeException {
    private final int code;
    
    public PaymentException(String message) {
        super(message);
        this.code = PaymentErrorCode.PAYMENT_ERROR.getCode();
    }
    
    public PaymentException(int code, String message) {
        super(message);
        this.code = code;
    }
    
    public PaymentException(String message, Throwable cause) {
        super(message, cause);
        this.code = PaymentErrorCode.PAYMENT_ERROR.getCode();
    }
    
    public PaymentException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    // 便捷的静态工厂方法
    public static PaymentException paymentCancelled() {
        return PaymentErrorCode.PAYMENT_CANCELLED.toException();
    }

    public static PaymentException paymentExpired() {
        return PaymentErrorCode.PAYMENT_EXPIRED.toException();
    }

    public static PaymentException paymentDeclined() {
        return PaymentErrorCode.PAYMENT_DECLINED.toException();
    }

    public static PaymentException invalidPaymentMethod() {
        return PaymentErrorCode.INVALID_PAYMENT_METHOD.toException();
    }

    public static PaymentException insufficientFunds() {
        return PaymentErrorCode.INSUFFICIENT_FUNDS.toException();
    }

    public static PaymentException paymentAlreadyProcessed() {
        return PaymentErrorCode.PAYMENT_ALREADY_PROCESSED.toException();
    }

    public static PaymentException invalidCurrency() {
        return PaymentErrorCode.INVALID_CURRENCY.toException();
    }

    public static PaymentException invalidAmount() {
        return PaymentErrorCode.INVALID_AMOUNT.toException();
    }

    public static PaymentException stripeApiError(String detail) {
        return PaymentErrorCode.STRIPE_API_ERROR.toException(detail);
    }

    public static PaymentException webhookVerificationFailed() {
        return PaymentErrorCode.WEBHOOK_VERIFICATION_FAILED.toException();
    }
    
    public int getCode() {
        return code;
    }
} 