package com.loqiu.moneykeeper.interceptor;

import com.loqiu.moneykeeper.common.TraceContext;
import com.loqiu.moneykeeper.constant.TraceConstant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class TraceIdInterceptorTest {

    private final TraceIdInterceptor interceptor = new TraceIdInterceptor();

    @AfterEach
    void tearDown() {
        TraceContext.clear();
    }

    @Test
    void preHandleShouldReuseIncomingTraceIdAndExposeIt() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(TraceConstant.TRACE_ID_HEADER, "trace-123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        interceptor.preHandle(request, response, new Object());

        assertEquals("trace-123", TraceContext.getTraceId());
        assertEquals("trace-123", request.getAttribute(TraceConstant.TRACE_ID_ATTRIBUTE));
        assertEquals("trace-123", response.getHeader(TraceConstant.TRACE_ID_HEADER));
    }

    @Test
    void preHandleShouldGenerateTraceIdWhenMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        interceptor.preHandle(request, response, new Object());

        String traceId = TraceContext.getTraceId();
        assertNotNull(traceId);
        assertEquals(traceId, request.getAttribute(TraceConstant.TRACE_ID_ATTRIBUTE));
        assertEquals(traceId, response.getHeader(TraceConstant.TRACE_ID_HEADER));

        interceptor.afterCompletion(request, response, new Object(), null);
        assertNull(TraceContext.getTraceId());
    }
}
