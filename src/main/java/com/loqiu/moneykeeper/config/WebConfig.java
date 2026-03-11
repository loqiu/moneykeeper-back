package com.loqiu.moneykeeper.config;

import com.loqiu.moneykeeper.interceptor.JwtAuthenticationInterceptor;
import com.loqiu.moneykeeper.interceptor.TraceIdInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtAuthenticationInterceptor jwtAuthenticationInterceptor;

    @Autowired
    private TraceIdInterceptor traceIdInterceptor;

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/api/notifications/subscribe/**")
                .allowedOrigins("https://money-keeper.com")
                .allowedMethods("GET")
                .allowCredentials(true)
                .exposedHeaders("*")
                .maxAge(3600);

        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .exposedHeaders("*")
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthenticationInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/auth/google",
                        "/api/payments/webhooks/stripe",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/error"
                );

        registry.addInterceptor(traceIdInterceptor)
                .addPathPatterns("/api/**");
    }
}
