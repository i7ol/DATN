package com.datn.shopnotification.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "push_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PushToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    @Column(unique = true)
    private String token;
    private String device;
}
