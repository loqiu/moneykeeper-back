package com.loqiu.moneykeeper.util;

import com.loqiu.moneykeeper.constant.ErrorKeyConstants;
import com.loqiu.moneykeeper.exception.BadRequestException;
import org.springframework.util.StringUtils;

import java.util.Locale;

public final class RecordTypeNormalizer {

    private static final String TYPE_INCOME = "income";
    private static final String TYPE_EXPENSE = "expense";

    private RecordTypeNormalizer() {
    }

    public static String normalizeRequired(String value, String missingMessage, String invalidMessage) {
        return normalizeRequired(value, missingMessage, invalidMessage, ErrorKeyConstants.COMMON_BAD_REQUEST);
    }

    public static String normalizeRequired(String value, String missingMessage, String invalidMessage, String errorKey) {
        if (!StringUtils.hasText(value)) {
            throw new BadRequestException(missingMessage, errorKey);
        }
        return normalizeInternal(value.trim(), invalidMessage, errorKey);
    }

    public static String normalizeOptional(String value, String invalidMessage) {
        return normalizeOptional(value, invalidMessage, ErrorKeyConstants.COMMON_BAD_REQUEST);
    }

    public static String normalizeOptional(String value, String invalidMessage, String errorKey) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return normalizeInternal(value.trim(), invalidMessage, errorKey);
    }

    private static String normalizeInternal(String value, String invalidMessage, String errorKey) {
        String normalizedValue = value.toLowerCase(Locale.ROOT);
        if (TYPE_INCOME.equals(normalizedValue)) {
            return TYPE_INCOME;
        }
        if (TYPE_EXPENSE.equals(normalizedValue)) {
            return TYPE_EXPENSE;
        }
        throw new BadRequestException(invalidMessage, errorKey);
    }
}
