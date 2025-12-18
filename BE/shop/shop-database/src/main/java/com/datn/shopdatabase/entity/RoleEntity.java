
package com.datn.shopdatabase.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.Set;

@Entity
@Table(name = "ROLES")
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

    // Thêm quan hệ với permissions
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "ROLE_PERMISSIONS",
            joinColumns = @JoinColumn(name = "role_name"),
            inverseJoinColumns = @JoinColumn(name = "permission_name")
    )
    Set<PermissionEntity> permissions;
}