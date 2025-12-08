package com.datn.shopdatabase.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cms_post_categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostCategoryEntity extends BaseEntity {
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

}
