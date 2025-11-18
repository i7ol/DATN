package com.datn.shopcms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "cms_banners")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Banner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=1000)
    private String imageUrl;

    @Column(length=1000)
    private String link;

    @Column(length=100)
    private String position;

    private Integer sortOrder = 0;

    private Boolean active = true;

    @CreationTimestamp
    @Column(updatable=false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
