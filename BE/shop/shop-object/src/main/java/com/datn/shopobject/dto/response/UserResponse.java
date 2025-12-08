package com.datn.shopobject.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

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
    String pushToken;
}
