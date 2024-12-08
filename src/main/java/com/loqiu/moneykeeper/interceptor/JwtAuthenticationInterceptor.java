package com.loqiu.moneykeeper.interceptor;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.loqiu.moneykeeper.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import com.auth0.jwt.interfaces.DecodedJWT;


@Component
public class JwtAuthenticationInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        // 放行登录接口
        if (request.getRequestURI().equals("/api/auth/login")) {
            return true;
        }

        // 获取token
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        try {
            // 验证token
            token = token.substring(7);
            DecodedJWT jwt = jwtUtil.verifyToken(token);
            
            // 将用户信息存入请求属性中
            request.setAttribute("userId", Long.parseLong(jwt.getSubject()));
            request.setAttribute("username", jwt.getClaim("username").asString());
            
            return true;
        } catch (JWTVerificationException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
    }
} 