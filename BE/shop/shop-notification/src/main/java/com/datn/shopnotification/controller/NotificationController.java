package com.datn.shopnotification.controller;

import com.datn.shopnotification.dto.request.NotificationRequest;
import com.datn.shopnotification.dto.response.NotificationResponse;
import com.datn.shopnotification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;

    // Tạo notification
    @PostMapping
    public ResponseEntity<NotificationResponse> create(@RequestBody NotificationRequest request) {
        Long userId = request.getReceiverId();
        if (userId == null) {
            userId = getCurrentUserId();
            request.setReceiverId(userId);
        }
        return ResponseEntity.ok(service.create(request));
    }

    // Lấy notification theo id
    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    // Lấy danh sách notification của chính mình (có phân trang)
    @GetMapping("/me")
    public ResponseEntity<Page<NotificationResponse>> myNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(service.listByUser(userId, PageRequest.of(page, size)));
    }

    // Đánh dấu đã đọc
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable("id") Long id) {
        service.markAsRead(id);
        return ResponseEntity.noContent().build();
    }

    // Đếm số notification chưa đọc
    @GetMapping("/me/unread-count")
    public ResponseEntity<Long> unreadCount() {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(service.unreadCount(userId));
    }

    // --------------------
    // Lấy userId từ SecurityContext
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new RuntimeException("User not authenticated");
        }
        // Giả sử filter JWT đặt principal là userId
        Object principal = auth.getPrincipal();
        if (principal instanceof Long) return (Long) principal;
        if (principal instanceof String) return Long.parseLong((String) principal);
        throw new RuntimeException("Cannot parse userId from authentication principal");
    }
}
