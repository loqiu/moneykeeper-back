package com.loqiu.moneykeeper.exception;

import com.loqiu.moneykeeper.common.TraceContext;
import com.loqiu.moneykeeper.constant.ErrorKeyConstants;
import com.loqiu.moneykeeper.constant.TraceConstant;
import com.loqiu.moneykeeper.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LogManager.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorized(UnauthorizedException ex, HttpServletRequest request) {
        return buildError(HttpStatus.UNAUTHORIZED, ex.getMessage(), request, null, ex.getErrorKey(), ex.getErrorParams());
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiErrorResponse> handleForbidden(ForbiddenException ex, HttpServletRequest request) {
        return buildError(HttpStatus.FORBIDDEN, ex.getMessage(), request, null, ex.getErrorKey(), ex.getErrorParams());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request, null, ex.getErrorKey(), ex.getErrorParams());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(ConflictException ex, HttpServletRequest request) {
        return buildError(HttpStatus.CONFLICT, ex.getMessage(), request, null, ex.getErrorKey(), ex.getErrorParams());
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ApiErrorResponse> handleServiceUnavailable(ServiceUnavailableException ex,
                                                                     HttpServletRequest request) {
        return buildError(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), request, null, ex.getErrorKey(), ex.getErrorParams());
    }

    @ExceptionHandler({BadRequestException.class, IllegalArgumentException.class, MethodArgumentNotValidException.class,
            MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class})
    public ResponseEntity<ApiErrorResponse> handleBadRequest(Exception ex, HttpServletRequest request) {
        String message = ex instanceof HttpMessageNotReadableException
                ? "Malformed JSON request"
                : ex.getMessage();
        String errorKey = ex instanceof HttpMessageNotReadableException
                ? ErrorKeyConstants.COMMON_MALFORMED_JSON
                : ex instanceof ApiBusinessException apiBusinessException
                ? apiBusinessException.getErrorKey()
                : null;
        Map<String, Object> errorParams = ex instanceof ApiBusinessException apiBusinessException
                ? apiBusinessException.getErrorParams()
                : Map.of();
        return buildError(HttpStatus.BAD_REQUEST, message, request, null, errorKey, errorParams);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        logger.error("Unhandled exception on path {}", request.getRequestURI(), ex);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", request, ex, null, Map.of());
    }

    private ResponseEntity<ApiErrorResponse> buildError(HttpStatus status,
                                                        String message,
                                                        HttpServletRequest request,
                                                        Exception ex,
                                                        String errorKey,
                                                        Map<String, Object> errorParams) {
        String traceId = resolveTraceId(request);
        String resolvedErrorKey = errorKey == null ? ErrorKeyConstants.defaultForStatus(status.value()) : errorKey;
        Map<String, Object> resolvedErrorParams = errorParams == null ? Map.of() : errorParams;
        if (ex != null) {
            logger.error("API request failed - status: {}, path: {}, traceId: {}, errorKey: {}, message: {}",
                    status.value(), request.getRequestURI(), traceId, resolvedErrorKey, message);
        } else {
            logger.warn("API request rejected - status: {}, path: {}, traceId: {}, errorKey: {}, message: {}",
                    status.value(), request.getRequestURI(), traceId, resolvedErrorKey, message);
        }
        ApiErrorResponse body = new ApiErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                resolvedErrorKey,
                resolvedErrorParams,
                message,
                request.getRequestURI(),
                LocalDateTime.now(),
                traceId
        );
        return ResponseEntity.status(status).body(body);
    }

    private String resolveTraceId(HttpServletRequest request) {
        String traceId = TraceContext.getTraceId();
        if (traceId != null) {
            return traceId;
        }
        Object requestTraceId = request.getAttribute(TraceConstant.TRACE_ID_ATTRIBUTE);
        return requestTraceId == null ? null : requestTraceId.toString();
    }
}
