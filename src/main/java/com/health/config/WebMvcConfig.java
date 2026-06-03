package com.health.config;

import com.health.handler.RateLimitInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // 个人项目无需 AI 接口限流，已禁用。如需恢复，取消下面代码注释即可
    // private final RateLimitInterceptor rateLimitInterceptor;
    //
    // public WebMvcConfig(RateLimitInterceptor rateLimitInterceptor) {
    //     this.rateLimitInterceptor = rateLimitInterceptor;
    // }
    //
    // @Override
    // public void addInterceptors(InterceptorRegistry registry) {
    //     registry.addInterceptor(rateLimitInterceptor).addPathPatterns("/api/ai/**");
    // }
}
