package com.datn.shopadmin.config;

import com.datn.shopauth.service.AuthenticationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AuthenticationService authService;

    public JwtAuthenticationFilter(AuthenticationService authService) {
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = authService.extractToken(request);

        if (token != null && authService.validateToken(token)) {
            var userDetails = authService.getUserFromToken(token);
            var auth = authService.buildAuthentication(userDetails, request);
            authService.setSecurityContext(auth);
        }

        filterChain.doFilter(request, response);
    }
}
