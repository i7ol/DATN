package com.datn.shopdatabase.entity;

import com.datn.shopnotification.enums.NotificationChannel;
import com.datn.shopnotification.enums.NotificationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long receiverId; // linked to userId
    private String receiverEmail;
    private String receiverPhone;
    private String receiverToken; // push token

    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "read_flag", nullable = false, columnDefinition = "NUMBER(1,0) DEFAULT 0")
    private boolean readFlag;


    @Enumerated(EnumType.STRING)
    private NotificationStatus status = NotificationStatus.PENDING;

    @Enumerated(EnumType.STRING)
    private NotificationChannel channel = NotificationChannel.EMAIL;

    @Column(name = "broadcast", nullable = false, columnDefinition = "NUMBER(1,0) DEFAULT 0")
    private boolean broadcast = false;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
