package com.datn.shopnotification.dto.request;

import com.datn.shopnotification.enums.NotificationChannel;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequest {
    private Long receiverId;
    private String title;
    private String message;
    private NotificationChannel channel;
    private boolean broadcast = false;
}
