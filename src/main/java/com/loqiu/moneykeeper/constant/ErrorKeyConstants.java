package com.loqiu.moneykeeper.constant;

public final class ErrorKeyConstants {

    public static final String COMMON_BAD_REQUEST = "common.bad_request";
    public static final String COMMON_UNAUTHORIZED = "common.unauthorized";
    public static final String COMMON_FORBIDDEN = "common.forbidden";
    public static final String COMMON_NOT_FOUND = "common.not_found";
    public static final String COMMON_CONFLICT = "common.conflict";
    public static final String COMMON_SERVICE_UNAVAILABLE = "common.service_unavailable";
    public static final String COMMON_INTERNAL_SERVER_ERROR = "common.internal_server_error";
    public static final String COMMON_MALFORMED_JSON = "common.malformed_json";

    public static final String AUTH_INVALID_CREDENTIALS = "auth.invalid_credentials";
    public static final String AUTH_USER_NOT_FOUND = "auth.user_not_found";
    public static final String AUTH_INVALID_TOKEN = "auth.invalid_token";
    public static final String AUTH_INVALID_AUTH_HEADER = "auth.invalid_authorization_header";
    public static final String AUTH_USERNAME_EXISTS = "auth.username_already_exists";
    public static final String AUTH_EMAIL_EXISTS = "auth.email_already_exists";
    public static final String AUTH_GOOGLE_ID_TOKEN_REQUIRED = "auth.google_id_token_required";
    public static final String AUTH_GOOGLE_LOGIN_FAILED = "auth.google_login_failed";

    public static final String CATEGORY_INVALID_TYPE = "category.invalid_type";
    public static final String RECORD_INVALID_TYPE = "record.invalid_type";
    public static final String RECORD_TYPE_MISMATCH = "record.type_mismatch";
    public static final String BUDGET_DUPLICATE_BUDGET = "budget.duplicate_budget";
    public static final String LEDGER_INVITE_ALREADY_MEMBER = "ledger.invite.already_member";
    public static final String LEDGER_INVITE_EMAIL_MISMATCH = "ledger.invite.email_mismatch";
    public static final String EXPORT_JOB_NOT_READY = "export.job_not_ready";

    private ErrorKeyConstants() {
    }

    public static String defaultForStatus(int statusCode) {
        return switch (statusCode) {
            case 400 -> COMMON_BAD_REQUEST;
            case 401 -> COMMON_UNAUTHORIZED;
            case 403 -> COMMON_FORBIDDEN;
            case 404 -> COMMON_NOT_FOUND;
            case 409 -> COMMON_CONFLICT;
            case 503 -> COMMON_SERVICE_UNAVAILABLE;
            default -> COMMON_INTERNAL_SERVER_ERROR;
        };
    }
}
