package com.datn.shopdatabase.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cms_media")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;
    private String url;
    private String contentType;

    @Column(name = "media_size")
    private Long size;
    private Boolean active;

}
