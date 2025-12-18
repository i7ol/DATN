
package com.datn.shopdatabase.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "PERMISSIONS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PermissionEntity extends BaseEntity {
    @Id
    @Column(name = "PERMISSION_NAME", length = 100, nullable = false)
    String name;

    @Column(name = "DESCRIPTION", length = 255)
    String description;

    @Column(name = "ENDPOINT_PATTERN", length = 200)
    String endpointPattern;

    @Column(name = "HTTP_METHOD", length = 10)
    String httpMethod;
}