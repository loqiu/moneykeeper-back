package com.loqiu.moneykeeper.enums;

import com.loqiu.moneykeeper.exception.PaymentException;

public enum PaymentErrorCode {

    // 支付相关错误码 (2000-2999)
    PAYMENT_ERROR(2000, "支付处理失败"),
    PAYMENT_CANCELLED(2001, "支付已取消"),
    PAYMENT_EXPIRED(2002, "支付已过期"),
    PAYMENT_DECLINED(2003, "支付被拒绝"),
    INVALID_PAYMENT_METHOD(2004, "无效的支付方式"),
    INSUFFICIENT_FUNDS(2005, "余额不足"),
    PAYMENT_ALREADY_PROCESSED(2006, "支付已处理"),
    INVALID_CURRENCY(2007, "无效的货币类型"),
    INVALID_AMOUNT(2008, "无效的金额"),
    STRIPE_API_ERROR(2009, "Stripe API调用失败"),
    WEBHOOK_VERIFICATION_FAILED(2010, "Webhook验证失败"),
    CREATE_PAYMENT_INTENT_FAILED(2011, "创建支付Intent失败"),
    COMFIRM_PAYMENT_INTENT_FAILED(2012, "确认支付Intent失败"),
    COMPLETE_PAYMENT_INTENT_FAILED(2012, "完成支付Intent失败"),
    CANCEL_PAYMENT_INTENT_FAILED(2013, "取消支付Intent失败"),
    REFUND_PAYMENT_INTENT_FAILED(2014, "退款支付Intent失败"),
    CREATE_SESSION_FAILED(2015, "创建Session失败"),
    WEBHOOD_EVENT_NOT_FOUND(2016, "Webhook事件未找到"),
    ;

    private final int code;
    private final String message;

    PaymentErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
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
