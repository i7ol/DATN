package com.datn.shopadmin.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class AdminSecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    private final String[] PUBLIC_ENDPOINTS = {
            "/auth/**",
            "/api/auth/**",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-ui/index.html",

            "/api/admin/products/**",
            "/api/admin/products",
            "/api/admin/products/{id}",

            "uploads/products",
            "uploads/products/**",

            "/api/admin/inventory",
            "/api/admin/inventory/**",
            "/api/admin/inventory/{variantId}",
            "/api/admin/inventory/import",
            "/api/admin/inventory/export",
            "/api/admin/inventory/reserve",
            "/api/admin/inventory/release",
            "/api/admin/inventory/deduct",
            "/api/admin/inventory/adjust/{variantId}",
            "/api/admin/inventory/transactions/{variantId}",

            "/api/admin/categories",
            "/api/admin/categories/**",

            "/api/admin/orders",
            "/api/admin/orders/{orderId}",
            "/api/admin/orders/{orderId}/status",
            "/api/admin/orders/{orderId}/payment",
            "/api/admin/payments/order/{orderId}",
            "/api/admin/payments/{paymentId}/paid",
            "/api/admin/payments/{paymentId}/refund",

            "/api/admin/shipping",
            "/api/admin/shipping/{id}/status",
            "/api/admin/shipping/{id}",
            "/api/admin/shipping/order/{orderId}",

            "/api/admin/users",
            "/api/admin/users/{id}",
            "/api/admin/users/{id}/role",
            "UNLOCK user",


    };

    @Bean
    public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
        cfg.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"));
        cfg.setAllowedHeaders(Arrays.asList("*")); // Cho phép tất cả headers
        cfg.setExposedHeaders(Arrays.asList("Authorization", "Refresh-Token", "Content-Type"));
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}