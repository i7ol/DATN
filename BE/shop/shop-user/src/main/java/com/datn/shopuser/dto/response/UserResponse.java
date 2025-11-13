package com.datn.shopuser.dto.response;

import com.datn.shopcore.entity.Role;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Set;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    Long id;
    String username;
    String email;
    String phone;
    String address;
    List<String> roles;
}
