package com.loqiu.moneykeeper.config;

import com.loqiu.moneykeeper.interceptor.JwtAuthenticationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtAuthenticationInterceptor jwtAuthenticationInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // 允许跨域访问的 API 路径
//                .allowedOrigins(
//                        "http://localhost:8080",
//                        "http://cindypig.com",
//                        "https://cindypig.com",
//                        "http://8.208.121.218:8080",
//                        "http://8.208.121.218",
//                        "https://8.208.121.218:8080",
//                        "https://8.208.121.218"
//                )  // 允许的源地址（即前端地址）
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE","HEAD","OPTIONS")  // 允许的请求方式
                .allowedHeaders("*")  // 允许的请求头
                .allowCredentials(true);  // 是否允许带有凭据（如 Cookies）
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthenticationInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login");
    }
}
