package com.datn.shopadmin.config;

import com.datn.shopauth.service.AuthenticationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {
        private final String[] PUBLIC_ENDPOINTS = {
//            "/auth/token",
//            "/auth/introspect",
//
            "/api/admin/categories",
            "/api/admin/categories/**",
//
            "/api/admin/products",
            "/api/admin/products/**",
//            "/api/user/products/**",
//            "/api/user/products",
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
//            "/api/carts",
//            "/api/carts/**",
//
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
//
//            "/api/shipping",
//            "/api/shipping/**",
//
//            "/api/cms/media",
//            "/uploads/**",
    };

    // Tạo bean JwtAuthenticationFilter, inject AuthenticationService trực tiếp
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(AuthenticationService authService) {
        return new JwtAuthenticationFilter(authService);
    }

    // SecurityFilterChain
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationService authService) throws Exception {
        http.csrf(cs -> cs.disable());

        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        http.sessionManagement(sm ->
                sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
        );

        // Sử dụng bean filter
        http.addFilterBefore(jwtAuthenticationFilter(authService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Cấu hình CORS
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

    // AuthenticationManager
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    // UserDetailsService mẫu
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            if ("admin".equals(username)) {
                return User.withUsername("admin")
                        .password("{noop}123456")
                        .roles("ADMIN")
                        .build();
            }
            throw new UsernameNotFoundException("User not found: " + username);
        };
    }
}
