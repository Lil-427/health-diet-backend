package com.health.config;

import com.health.handler.JwtAuthenticationTokenFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置
 * 无状态会话、JWT 认证、放行认证接口
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    public SecurityConfig(JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter) {
        this.jwtAuthenticationTokenFilter = jwtAuthenticationTokenFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 启用 CORS（使用 CorsConfig 配置）
                .cors().and()
                // 禁用 CSRF（前后端分离架构不需要）
                .csrf().disable()
                // 无状态 Session（不使用 Session 存储用户信息）
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                // 配置接口权限
                .authorizeRequests()
                .antMatchers("/api/auth/**").permitAll()   // 放行认证接口
                .antMatchers("/uploads/**").permitAll()   // 放行静态资源
                .anyRequest().authenticated()               // 其他接口需要登录
                .and()
                // 在 UsernamePasswordAuthenticationFilter 前添加 JWT 过滤器
                .addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt 密码编码器
        return new BCryptPasswordEncoder();
    }
}
