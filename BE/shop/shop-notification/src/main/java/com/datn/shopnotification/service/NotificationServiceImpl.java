package com.datn.shopnotification.service;

import com.datn.shopdatabase.entity.NotificationEntity;
import com.datn.shopdatabase.entity.NotificationSettingEntity;
import com.datn.shopobject.dto.request.NotificationRequest;
import com.datn.shopobject.dto.response.NotificationResponse;
import com.datn.shopdatabase.enums.NotificationChannel;
import com.datn.shopdatabase.enums.NotificationStatus;
import com.datn.shopdatabase.repository.NotificationRepository;
import com.datn.shopdatabase.entity.UserEntity;
import com.datn.shopuser.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private  NotificationRepository repository;
    private  MailService mailService;
    private  SmsService smsService;
    private  FirebaseService firebaseService;
    private  UserService userService; // Dùng service từ shop-user
    private  NotificationSettingService settingService;

    @Override
    @Transactional
    public NotificationResponse create(NotificationRequest request) {

        // Broadcast
        if (Boolean.TRUE.equals(request.isBroadcast())) {
            int count = broadcastNotification(request);

            return NotificationResponse.builder()
                    .id(null)
                    .receiverId(null)
                    .title(request.getTitle())
                    .message(request.getMessage())
                    .channel(request.getChannel().name())
                    .broadcast(true)
                    .status("BROADCASTED")
                    .messageCount(count) // Thêm số lượng gửi
                    .build();
        }

        // Normal notification
        UserEntity user = userService.getById(request.getReceiverId());
        NotificationEntity notification = buildNotification(user, request, false);

        NotificationEntity saved = repository.save(notification);
        return toResponse(saved);
    }


    private int broadcastNotification(NotificationRequest request) {
        List<UserEntity> users = userService.getAllUserEntities();
        int count = 0;

        for (UserEntity u : users) {
            NotificationEntity n = buildNotification(u, request, true);
            repository.save(n);
            count++;
        }

        return count;
    }


    private NotificationEntity buildNotification(UserEntity user, NotificationRequest request, boolean broadcast) {
        return NotificationEntity.builder()
                .receiverId(user.getId())
                .receiverEmail(user.getEmail())
                .receiverPhone(user.getPhone())
                .receiverToken(user.getPushToken())
                .title(request.getTitle())
                .message(request.getMessage())
                .channel(request.getChannel() == null ? NotificationChannel.EMAIL : request.getChannel())
                .status(NotificationStatus.PENDING)
                .broadcast(broadcast)
                .build();
    }

    @Override
    public NotificationResponse getById(Long id) {
        NotificationEntity n = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        return toResponse(n);
    }

    @Override
    public Page<NotificationResponse> listByUser(Long userId, Pageable pageable) {
        return repository.findAllByReceiverIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public void markAsRead(Long id) {
        NotificationEntity n = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        if (!n.isReadFlag()) {
            n.setReadFlag(true);
            repository.save(n);
        }
    }

    @Override
    public long unreadCount(Long userId) {
        return repository.findAllByReceiverIdAndReadFlagFalse(userId).size();
    }

    @Override
    @Transactional
    public int processPendingNotifications() {
        List<NotificationEntity> pending = repository.findAllByStatus(NotificationStatus.PENDING);
        for (NotificationEntity n : pending) send(n);
        return pending.size();
    }

    @Override
    @Transactional
    public void send(NotificationEntity notification) {
        NotificationSettingEntity setting = settingService.getByUser(notification.getReceiverId());

        try {
            switch (notification.getChannel()) {
                case EMAIL -> {
                    if (setting.isEmailEnabled() && notification.getReceiverEmail() != null) {
                        mailService.sendEmail(notification.getReceiverEmail(), notification.getTitle(), notification.getMessage());
                    }
                }
                case SMS -> {
                    if (setting.isSmsEnabled() && notification.getReceiverPhone() != null) {
                        smsService.sendSms(notification.getReceiverPhone(), notification.getMessage());
                    }
                }
                case PUSH -> {
                    if (setting.isPushEnabled() && notification.getReceiverToken() != null) {
                        firebaseService.sendPush(notification.getReceiverToken(), notification.getTitle(), notification.getMessage());
                    }
                }
            }
            notification.setStatus(NotificationStatus.SENT);
        } catch (Exception e) {
            log.error("Failed to send notification {}: {}", notification.getId(), e.getMessage());
            notification.setStatus(NotificationStatus.FAILED);
        } finally {
            repository.save(notification);
        }
    }

    private NotificationResponse toResponse(NotificationEntity n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .receiverId(n.getReceiverId())
                .title(n.getTitle())
                .message(n.getMessage())
                .readFlag(n.isReadFlag())
                .status(n.getStatus().name())
                .channel(n.getChannel().name())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
