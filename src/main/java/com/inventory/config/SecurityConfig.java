package com.inventory.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)  // 关闭 CSRF
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()           // 所有请求放行，暂时不拦截
            )
            .formLogin(AbstractHttpConfigurer::disable)  // 关闭默认登录页
            .httpBasic(AbstractHttpConfigurer::disable); // 关闭 HTTP Basic
        return http.build();
    }
}