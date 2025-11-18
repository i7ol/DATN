package com.datn.shopnotification.service;

import com.datn.shopnotification.dto.request.NotificationRequest;
import com.datn.shopnotification.dto.response.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    NotificationResponse create(NotificationRequest request);
    NotificationResponse getById(Long id);
    Page<NotificationResponse> listByUser(Long userId, Pageable pageable);
    void markAsRead(Long id);
    long unreadCount(Long userId);
    int processPendingNotifications();
    void send(com.datn.shopnotification.entity.Notification notification);
}
