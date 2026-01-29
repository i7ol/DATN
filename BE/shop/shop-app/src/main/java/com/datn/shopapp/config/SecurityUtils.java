package com.datn.shopapp.config;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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
        return getPrincipal().getId();
    }

    public static String getCurrentUsername() {
        return getPrincipal().getUsername();
    }

//    public static String getCurrentEmail() {
//        return getPrincipal().getEmail();
//    }
//
//    public static List<String> getCurrentRoles() {
//        return getPrincipal().getAuthorities()
//                .stream()
//                .map(GrantedAuthority::getAuthority)
//                .toList();
//    }

}


