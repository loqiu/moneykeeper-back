package com.loqiu.moneykeeper.interceptor;

import com.loqiu.moneykeeper.common.TraceContext;
import com.loqiu.moneykeeper.constant.TraceConstant;
import com.loqiu.moneykeeper.util.TraceIdUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TraceIdInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String traceId = request.getHeader(TraceConstant.TRACE_ID_HEADER);
        if (!StringUtils.hasText(traceId)) {
            traceId = TraceIdUtil.generateTraceId();
        }
        traceId = traceId.trim();
        request.setAttribute(TraceConstant.TRACE_ID_ATTRIBUTE, traceId);
        response.setHeader(TraceConstant.TRACE_ID_HEADER, traceId);
        TraceContext.setTraceId(traceId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TraceContext.clear();
    }
}
