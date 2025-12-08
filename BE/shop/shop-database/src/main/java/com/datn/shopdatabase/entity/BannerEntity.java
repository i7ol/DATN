package com.datn.shopdatabase.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cms_banners")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BannerEntity extends BaseEntity {
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


}
