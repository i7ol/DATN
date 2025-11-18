package com.datn.shopcms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "cms_post_categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=255)
    private String name;

    @Column(nullable=false, unique=true, length=255)
    private String slug;

    @Column(length=1000)
    private String description;

    private Boolean active = true;

    @CreationTimestamp
    @Column(updatable=false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
