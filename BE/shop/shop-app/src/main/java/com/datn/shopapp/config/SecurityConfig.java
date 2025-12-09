package com.datn.shopapp.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final String[] PUBLIC_ENDPOINTS = {
//            "/auth/token",
//            "/auth/introspect",
//

            "/api/user/products/**",
            "/api/user/products",
//
//            "/api/users",
//            "/api/users/**",
//
//            "/api/payments",
//            "/api/payments/**",
//            "/api/payments/create",
//            "/api/payments/update-status",
//            "/api/payments/update-status/**",
//
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-ui/index.html",
//
//            "/api/orders",
//            "/api/orders/**",
//
            "/api/user/cart",
            "/api/user/cart/add",
            "/api/user/cart/total",
            "/api/user/cart/update",
            "/api/user/cart/remove",
            "/api/user/cart/merge",
            "/api/user/cart/**",
//
//            "/api/inventory",
//            "/api/inventory/**",
//
//            "/api/shipping",
//            "/api/shipping/**",
//
//            "/api/cms/media",
//            "/uploads/**",
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(cs -> cs.disable());

        // CORS configuration
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        // Stateless session
        http.sessionManagement(sm ->
                sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        // Các endpoint public
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .anyRequest().authenticated()
        );

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of("http://localhost:4200"));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg)
            throws Exception {
        return cfg.getAuthenticationManager();
    }
}
