package com.datn.shopauth.config;


import com.datn.shopauth.service.AuthenticationService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AuthenticationService authenticationService;

    // Danh sách public endpoints
    private final String[] PUBLIC_ENDPOINTS = {
            "/auth/token",
            "/auth/introspect",

            "/api/categories",
            "/api/categories/**",

            "/api/products",
            "/api/products/**",

            "/api/users",
            "/api/users/**",

            "/api/payments",
            "/api/payments/**",
            "/api/payments/create",
            "/api/payments/update-status",
            "/api/payments/update-status/**",

            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs.yaml",
            "/api/orders",
            "/api/orders/**",

            "/api/carts",
            "/api/carts/**",

            "/api/inventory",
            "/api/inventory/**",

            "/api/shipping",
            "/api/shipping/**",

            "/api/cms/media",
            "/uploads/**",
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Bỏ qua OPTIONS
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // Bỏ qua public endpoints
        for (String endpoint : PUBLIC_ENDPOINTS) {
            String regex = endpoint.replace("**", ".*");
            if (path.matches(regex)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        // Check header Authorization
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            // Không token → return 401 nếu cần
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String token = header.substring(7);

        if (!authenticationService.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String username = authenticationService.extractUsername(token);
        List<String> roles = authenticationService.extractRoles(token);

        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(username, null, authorities);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }}



