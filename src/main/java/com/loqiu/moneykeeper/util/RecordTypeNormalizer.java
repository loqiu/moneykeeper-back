package com.loqiu.moneykeeper.util;

import com.loqiu.moneykeeper.exception.BadRequestException;
import org.springframework.util.StringUtils;

import java.util.Locale;

public final class RecordTypeNormalizer {

    private static final String TYPE_INCOME = "income";
    private static final String TYPE_EXPENSE = "expense";
    private static final String TYPE_INCOME_CN = "收入";
    private static final String TYPE_EXPENSE_CN = "支出";

    private RecordTypeNormalizer() {
    }

    public static String normalizeRequired(String value, String missingMessage, String invalidMessage) {
        if (!StringUtils.hasText(value)) {
            throw new BadRequestException(missingMessage);
        }
        return normalizeInternal(value.trim(), invalidMessage);
    }

    public static String normalizeOptional(String value, String invalidMessage) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return normalizeInternal(value.trim(), invalidMessage);
    }

    private static String normalizeInternal(String value, String invalidMessage) {
        if (TYPE_INCOME_CN.equals(value)) {
            return TYPE_INCOME;
        }
        if (TYPE_EXPENSE_CN.equals(value)) {
            return TYPE_EXPENSE;
        }

        String normalizedValue = value.toLowerCase(Locale.ROOT);
        if (TYPE_INCOME.equals(normalizedValue)) {
            return TYPE_INCOME;
        }
        if (TYPE_EXPENSE.equals(normalizedValue)) {
            return TYPE_EXPENSE;
        }

        throw new BadRequestException(invalidMessage);
    }
}
