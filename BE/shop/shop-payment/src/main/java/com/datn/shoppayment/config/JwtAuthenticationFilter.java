package com.datn.shoppayment.config;

import com.datn.shoppayment.client.AuthPaymentClient;
import com.datn.shopobject.dto.response.UserInfoResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.datn.shopobject.security.UserPrincipal;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AuthPaymentClient authPaymentClient;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        // Chỉ skip các endpoint thực sự public (không cần auth)
        return path.startsWith("/api/payments/vnpay/return")
                || path.startsWith("/api/payments/guest")     // nếu có guest payment
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            log.info(">>> Payment service received Bearer token: {}",
                    authHeader.substring(0, Math.min(100, authHeader.length())) + "...");

            try {
                // Gọi Auth service để validate + lấy user info
                boolean isValid = authPaymentClient.validateToken(authHeader);

                if (isValid) {
                    UserInfoResponse userInfo = authPaymentClient.getCurrentUser(authHeader);

                    if (userInfo != null) {
                        UserPrincipal principal = new UserPrincipal(
                                userInfo.getId(),
                                userInfo.getUsername(),
                                userInfo.getRoles()
                        );

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(principal, null,
                                        userInfo.getRoles().stream()
                                                .map(SimpleGrantedAuthority::new)
                                                .collect(Collectors.toList()));

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.info("✅ Principal set successfully in Payment service: {} (id={})",
                                userInfo.getUsername(), userInfo.getId());
                    }
                } else {
                    log.warn("Token validation failed from auth-service");
                }
            } catch (Exception e) {
                log.error("Error in JWT filter (Payment service)", e);
            }
        } else {
            log.info("No Bearer token found in Payment service request");
        }

        chain.doFilter(request, response);
    }


//    @Override
//    protected void doFilterInternal(
//            HttpServletRequest request,
//            HttpServletResponse response,
//            FilterChain filterChain)
//            throws ServletException, IOException {
//
//        String authHeader = request.getHeader("Authorization");
//
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        String token = authHeader.substring(7);
//
//        try {
//            boolean isValid = authClient.validateToken("Bearer " + token);
//            if (!isValid) {
//                filterChain.doFilter(request, response);
//                return;
//            }
//
//            UserInfoResponse userInfo =
//                    authClient.getCurrentUser("Bearer " + token);
//
//            List<GrantedAuthority> authorities = userInfo.getRoles()
//                    .stream()
//                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
//                    .collect(Collectors.toList());
//
//            UserPrincipal principal = new UserPrincipal(
//                    userInfo.getId(),
//                    userInfo.getUsername(),
//                    userInfo.getRoles()
//            );
//
//            UsernamePasswordAuthenticationToken authentication =
//                    new UsernamePasswordAuthenticationToken(
//                            principal,
//                            null,
//                            authorities
//                    );
//
//            authentication.setDetails(
//                    new WebAuthenticationDetailsSource().buildDetails(request)
//            );
//
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//
//        } catch (Exception e) {
//            log.error("JWT authentication failed", e);
//        }
//
//        filterChain.doFilter(request, response);
//    }
}
