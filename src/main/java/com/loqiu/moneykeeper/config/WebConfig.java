package com.loqiu.moneykeeper.config;

import com.loqiu.moneykeeper.interceptor.JwtAuthenticationInterceptor;
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

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        // SSE 端点的特殊 CORS 配置
        registry.addMapping("/api/notifications/subscribe/**")
                .allowedOrigins("https://cindypig.com")  // 严格指定允许的源
                .allowedMethods("GET")  // SSE 只需要 GET
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
                .excludePathPatterns("/api/auth/login")
                .excludePathPatterns("/**", "OPTIONS");
    }
}
