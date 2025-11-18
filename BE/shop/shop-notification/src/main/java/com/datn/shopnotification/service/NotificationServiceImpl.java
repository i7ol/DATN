package com.datn.shopnotification.service;

import com.datn.shopnotification.dto.request.NotificationRequest;
import com.datn.shopnotification.dto.response.NotificationResponse;
import com.datn.shopnotification.entity.*;
import com.datn.shopnotification.enums.NotificationChannel;
import com.datn.shopnotification.enums.NotificationStatus;
import com.datn.shopnotification.repository.NotificationRepository;
import com.datn.shopnotification.service.NotificationSettingService;
import com.datn.shopcore.entity.User;
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

    private final NotificationRepository repository;
    private final MailService mailService;
    private final SmsService smsService;
    private final FirebaseService firebaseService;
    private final UserService userService; // Dùng service từ shop-user
    private final NotificationSettingService settingService;

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
        User user = userService.getById(request.getReceiverId());
        Notification notification = buildNotification(user, request, false);

        Notification saved = repository.save(notification);
        return toResponse(saved);
    }


    private int broadcastNotification(NotificationRequest request) {
        List<User> users = userService.getAllUserEntities();
        int count = 0;

        for (User u : users) {
            Notification n = buildNotification(u, request, true);
            repository.save(n);
            count++;
        }

        return count;
    }


    private Notification buildNotification(User user, NotificationRequest request, boolean broadcast) {
        return Notification.builder()
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
        Notification n = repository.findById(id)
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
        Notification n = repository.findById(id)
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
        List<Notification> pending = repository.findAllByStatus(NotificationStatus.PENDING);
        for (Notification n : pending) send(n);
        return pending.size();
    }

    @Override
    @Transactional
    public void send(Notification notification) {
        NotificationSetting setting = settingService.getByUser(notification.getReceiverId());

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

    private NotificationResponse toResponse(Notification n) {
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
