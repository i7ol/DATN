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
    Integer provinceCode;
    Integer districtCode;
    Integer wardCode;
    List<String> roles;
    String pushToken;
}
