package com.datn.shopnotification.repository;

import com.datn.shopnotification.entity.Notification;
import com.datn.shopnotification.enums.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findAllByReceiverIdOrderByCreatedAtDesc(Long receiverId, Pageable pageable);
    List<Notification> findAllByReceiverIdAndReadFlagFalse(Long receiverId);
    List<Notification> findAllByStatus(NotificationStatus status);
}
