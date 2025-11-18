package com.datn.shopnotification.controller;

import com.datn.shopnotification.entity.NotificationSetting;
import com.datn.shopnotification.service.NotificationSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notification/settings")
@RequiredArgsConstructor
public class NotificationSettingController {

    private final NotificationSettingService service;

    // Lấy setting của chính mình
    @GetMapping("/me")
    public ResponseEntity<NotificationSetting> getMySettings() {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(service.getByUser(userId));
    }

    // Cập nhật setting của chính mình
    @PutMapping("/me")
    public ResponseEntity<NotificationSetting> updateMySettings(@RequestBody NotificationSetting setting) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(service.update(userId, setting));
    }

    // --------------------
    // Lấy userId từ SecurityContext
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new RuntimeException("User not authenticated");
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof Long) return (Long) principal;
        if (principal instanceof String) return Long.parseLong((String) principal);
        throw new RuntimeException("Cannot parse userId from authentication principal");
    }
}
