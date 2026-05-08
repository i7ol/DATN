package com.datn.shopdatabase.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "return_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReturnImageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_id", nullable = false)
    private OrderReturnEntity orderReturn;

    @Column(nullable = false)
    private String imageUrl;
}