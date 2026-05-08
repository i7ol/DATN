package com.datn.shoporder.config;

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

@Configuration
@RequiredArgsConstructor
public class OrderSecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final String[] PUBLIC_ENDPOINTS = {
            "/api/user/orders",
            "/api/user/orders/my-orders",
            "/api/user/orders/my-orders/**",
            "/api/user/orders/checkout",
            "/api/user/orders/{orderId}/cancel",

            "/api/returns/**",
            "/api/returns/my-returns",
            "/api/returns/{returnId}",

            "/api/admin/orders",
            "/api/admin/orders/**",
            "/api/admin/orders/statistics/revenue",
            "/api/admin/orders/{orderId}/delivered",
            "/api/admin/orders/{orderId}/complete",
            "/api/admin/orders/statistics/top-products",
            "/api/admin/orders/statistics/revenue-by-date",
            "/api/admin/orders/statistics/summary",

            "/api/admin/returns/**",
            "/api/admin/returns/pending",
            "/api/admin/returns/{returnId}",
            "/api/admin/returns/{returnId}/approve",
            "/api/admin/returns/{returnId}/reject",
            "/api/admin/returns/{returnId}/complete",



            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-ui/index.html"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(cs -> cs.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/internal/**").permitAll()

                        // Cho phép Guest checkout
                        .requestMatchers(HttpMethod.POST, "/api/user/orders/checkout").permitAll()
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        // Các endpoint user khác cần auth
                        .requestMatchers("/api/user/orders/my-orders/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/user/orders/{orderId}/cancel").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/user/orders/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/admin/orders/statistics/revenue").hasRole("ADMIN")
                        .requestMatchers("/api/admin/orders/**").hasRole("ADMIN")
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/actuator/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(Arrays.asList("http://localhost:4200")); // FE domain
        cfg.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        cfg.setAllowedHeaders(Arrays.asList("*"));
        cfg.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
