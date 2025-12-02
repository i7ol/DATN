package com.datn.shopdatabase.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "cms_posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=500)
    private String title;

    @Column(nullable=false, unique=true, length=500)
    private String slug;

    @Column(length=2000)
    private String summary;

    @Lob
    private String content;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private PostCategory category;

    private String author;

    private Instant publishedAt;

    private Boolean active = true;

    @CreationTimestamp
    @Column(updatable=false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
