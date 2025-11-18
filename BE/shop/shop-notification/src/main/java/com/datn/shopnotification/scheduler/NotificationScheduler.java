package com.datn.shopnotification.scheduler;

import com.datn.shopnotification.entity.Notification;
import com.datn.shopnotification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final NotificationService notificationService;

    // Chạy mỗi 1 phút
    @Scheduled(fixedRate = 60000)
    public void processPendingNotifications() {
        try {
            // processPendingNotifications() đã trả về số lượng notifications đã xử lý
            int processed = notificationService.processPendingNotifications();
            if (processed > 0) log.info("Processed {} pending notifications", processed);
        } catch (Exception e) {
            log.error("Error in notification scheduler: {}", e.getMessage(), e);
        }
    }
}


