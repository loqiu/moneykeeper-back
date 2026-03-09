package com.loqiu.moneykeeper.util;

import com.loqiu.moneykeeper.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;

public final class RequestAuthUtil {
    public static final String CURRENT_USER_ID = "currentUserId";
    public static final String CURRENT_USER_PIN = "currentUserPin";
    public static final String CURRENT_USERNAME = "currentUsername";
    public static final String CURRENT_USER_ROLE = "currentUserRole";
    public static final String ROLE_ADMIN = "admin";

    private RequestAuthUtil() {
    }

    public static Long getCurrentUserId(HttpServletRequest request) {
        Object value = request.getAttribute(CURRENT_USER_ID);
        if (value instanceof Long longValue) {
            return longValue;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            return Long.parseLong(text);
        }
        return null;
    }

    public static Long requireCurrentUserId(HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        if (currentUserId == null) {
            throw new UnauthorizedException("User context is missing");
        }
        return currentUserId;
    }

    public static String getCurrentUserPin(HttpServletRequest request) {
        Object value = request.getAttribute(CURRENT_USER_PIN);
        return value == null ? null : value.toString();
    }

    public static String getCurrentUsername(HttpServletRequest request) {
        Object value = request.getAttribute(CURRENT_USERNAME);
        return value == null ? null : value.toString();
    }

    public static String requireCurrentUsername(HttpServletRequest request) {
        String username = getCurrentUsername(request);
        if (username == null || username.isBlank()) {
            throw new UnauthorizedException("User context is missing");
        }
        return username;
    }

    public static String getCurrentUserRole(HttpServletRequest request) {
        Object value = request.getAttribute(CURRENT_USER_ROLE);
        return value == null ? null : value.toString();
    }

    public static boolean isAdmin(HttpServletRequest request) {
        String role = getCurrentUserRole(request);
        return role != null && ROLE_ADMIN.equalsIgnoreCase(role);
    }

    public static boolean isSelfOrAdmin(HttpServletRequest request, Long userId) {
        Long currentUserId = getCurrentUserId(request);
        return currentUserId != null && (currentUserId.equals(userId) || isAdmin(request));
    }
}