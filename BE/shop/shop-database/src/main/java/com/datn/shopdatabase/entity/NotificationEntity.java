package com.datn.shopdatabase.entity;

import com.datn.shopdatabase.enums.NotificationChannel;
import com.datn.shopdatabase.enums.NotificationStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long receiverId; // linked to userId
    private String receiverEmail;
    private String receiverPhone;
    private String receiverToken; // push token

    private String title;

    @Column(name = "MESSAGE", columnDefinition = "CLOB")
    private String message;

    @Column(name = "read_flag", nullable = false, columnDefinition = "NUMBER(1,0) DEFAULT 0")
    private boolean readFlag;


    @Enumerated(EnumType.STRING)
    private NotificationStatus status = NotificationStatus.PENDING;

    @Enumerated(EnumType.STRING)
    private NotificationChannel channel = NotificationChannel.EMAIL;

    @Column(name = "broadcast", nullable = false, columnDefinition = "NUMBER(1,0) DEFAULT 0")
    private boolean broadcast = false;


}
