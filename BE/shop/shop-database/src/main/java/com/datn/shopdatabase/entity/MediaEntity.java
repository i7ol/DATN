package com.datn.shopdatabase.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "cms_media")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Media {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;
    private String url;
    private String contentType;

    @Column(name = "media_size")
    private Long size;
    private Boolean active;
    @CreationTimestamp
    @Column(updatable=false)
    private Instant createdAt;
    @UpdateTimestamp
    private Instant updatedAt;
}
