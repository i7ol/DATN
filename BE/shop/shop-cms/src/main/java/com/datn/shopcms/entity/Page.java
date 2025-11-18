package com.datn.shopcms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "cms_pages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Page {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true, length=150)
    private String slug;

    @Column(nullable=false, length=255)
    private String title;

    @Column(length=1000)
    private String summary;

    @Lob
    private String content;

    @Column(length=255)
    private String metaTitle;

    @Column(length=1000)
    private String metaDescription;

    private Boolean active = true;

    @CreationTimestamp
    @Column(updatable=false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
