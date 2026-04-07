package com.datn.shopapp.config;

import com.datn.shopobject.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
@Slf4j
public final class SecurityUtils {

    private SecurityUtils() {}

    private static UserPrincipal getPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            throw new IllegalStateException("User not authenticated");
        }
        return principal;
    }

    public static Long getCurrentUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            if (auth == null || auth.getPrincipal() == null) {
                log.warn("SecurityContext is empty or no principal");
                return null;
            }

            Object principal = auth.getPrincipal();

            if (principal instanceof UserPrincipal userPrincipal) {
                return userPrincipal.getId();
            }

            // Trường hợp principal là String (username) - fallback
            if (principal instanceof String username) {
                log.warn("Principal is String, not UserPrincipal: {}", username);
                return null;
            }

            log.warn("Unknown principal type: {}", principal.getClass().getName());
            return null;

        } catch (Exception e) {
            log.error("Error getting current user id", e);
            return null;
        }
    }

    public static String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            return principal.getUsername();
        }
        return null;
    }

}


