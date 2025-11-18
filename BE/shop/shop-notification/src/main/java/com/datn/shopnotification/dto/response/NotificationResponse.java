package com.datn.shopnotification.dto.response;

import lombok.*;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private Long id;
    private Long receiverId;
    private String title;
    private String message;
    private boolean readFlag;
    private String status;
    private String channel;
    private Boolean broadcast;
    private int messageCount;

    private Instant createdAt;
}
