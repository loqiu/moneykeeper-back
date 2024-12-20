package com.loqiu.moneykeeper.common;

import com.loqiu.moneykeeper.constant.TraceConstant;
import org.apache.logging.log4j.ThreadContext;

public class TraceContext {

    public static void setTraceId(String traceId) {
        ThreadContext.put(TraceConstant.TRACE_ID_HEADER, traceId);
    }

    public static String getTraceId() {
        return ThreadContext.get(TraceConstant.TRACE_ID_HEADER);
    }

    public static void removeTraceId() {
        ThreadContext.remove(TraceConstant.TRACE_ID_HEADER);
    }

    public static void clear() {
        ThreadContext.clearAll();
    }

}
