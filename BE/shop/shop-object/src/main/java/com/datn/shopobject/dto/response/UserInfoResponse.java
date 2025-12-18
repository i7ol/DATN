
package com.datn.shopobject.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserInfoResponse {
    Long id;
    String username;
    String email;
    String phone;
    String address;
    List<String> roles;
    List<String> permissions;
}