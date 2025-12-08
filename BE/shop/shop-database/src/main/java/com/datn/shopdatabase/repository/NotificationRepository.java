package com.datn.shopdatabase.repository;

import com.datn.shopdatabase.entity.NotificationEntity;
import com.datn.shopdatabase.enums.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    Page<NotificationEntity> findAllByReceiverIdOrderByCreatedAtDesc(Long receiverId, Pageable pageable);
    List<NotificationEntity> findAllByReceiverIdAndReadFlagFalse(Long receiverId);
    List<NotificationEntity> findAllByStatus(NotificationStatus status);
}
