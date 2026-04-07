package com.datn.shopapp.config;

import com.datn.shopapp.client.AuthUserClient;
import com.datn.shopobject.dto.response.UserInfoResponse;
import com.datn.shopobject.security.UserPrincipal;
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

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AuthUserClient authClient;   // Feign client gọi đến auth-service (validate + /me)

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        // Bỏ qua các endpoint không cần auth
        return path.startsWith("/api/internal/") ||
                path.startsWith("/public/") ||
                path.contains("/actuator") ||
                path.startsWith("/swagger");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        try {
            boolean isValid = authClient.validateToken(authHeader);
            if (!isValid) {
                log.warn("Token validation FAILED from auth-service. Token: {}",
                        authHeader.substring(0, Math.min(80, authHeader.length())));
                // Vẫn cho phép tiếp tục (để Guest hoạt động)
                chain.doFilter(request, response);
                return;
            }

            // 2. Lấy full user info từ auth-service (/me)
            UserInfoResponse userInfo = authClient.getCurrentUser(authHeader);

            // 3. Build authorities
            List<GrantedAuthority> authorities = userInfo.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());

            // 4. Tạo principal
            UserPrincipal principal = new UserPrincipal(
                    userInfo.getId(),
                    userInfo.getUsername(),
                    userInfo.getRoles()
            );

            // 5. Set authentication
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principal, null, authorities);

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.info("User authenticated successfully: {} (id={}, roles={})",
                    userInfo.getUsername(), userInfo.getId(), userInfo.getRoles());

        } catch (Exception e) {
            log.error("JWT Filter Error - Token: {} | Error: {}",
                    authHeader.substring(0, Math.min(60, authHeader.length())), e.getMessage(), e);
            SecurityContextHolder.clearContext();
        }

        // Luôn tiếp tục filter chain dù thành công hay thất bại
        chain.doFilter(request, response);
    }
}