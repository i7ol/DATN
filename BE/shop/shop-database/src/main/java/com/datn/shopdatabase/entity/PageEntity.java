package com.datn.shopdatabase.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cms_pages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageEntity extends BaseEntity {
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

}
