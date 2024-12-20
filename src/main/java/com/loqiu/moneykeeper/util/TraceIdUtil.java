package com.loqiu.moneykeeper.util;

import java.util.UUID;

public class TraceIdUtil {

    public static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

}
