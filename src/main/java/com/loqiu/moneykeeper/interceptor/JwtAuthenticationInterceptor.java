package com.loqiu.moneykeeper.interceptor;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.loqiu.moneykeeper.util.JwtUtil;
import com.loqiu.moneykeeper.util.RequestAuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtAuthenticationInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return false;
        }

        try {
            DecodedJWT jwt = jwtUtil.verifyToken(token.substring(7));
            Long currentUserId = jwt.getClaim("userId").asLong();
            if (currentUserId == null) {
                throw new JWTVerificationException("Missing userId claim");
            }

            request.setAttribute(RequestAuthUtil.CURRENT_USER_ID, currentUserId);
            request.setAttribute(RequestAuthUtil.CURRENT_USER_PIN, jwt.getSubject());
            request.setAttribute(RequestAuthUtil.CURRENT_USERNAME, jwt.getClaim("username").asString());
            request.setAttribute(RequestAuthUtil.CURRENT_USER_ROLE, jwt.getClaim("role").asString());
            return true;
        } catch (JWTVerificationException e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return false;
        }
    }
}
