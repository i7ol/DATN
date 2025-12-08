package com.datn.shopdatabase.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "ROLES") // đổi tên bảng để tránh từ khóa trùng
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoleEntity extends BaseEntity {
    @Id
    @Column(name = "ROLE_NAME", length = 50, nullable = false)
    String name;

    @Column(name = "DESCRIPTION", length = 255)
    String description;
}
