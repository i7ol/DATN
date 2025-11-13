package com.datn.shopuser.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {
    @NotBlank(message = "Username cannot be empty")
    @Size(min = 3, message = "USERNAME_INVALID")
    String username;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 3, message = "INVALID_PASSWORD")
    String password;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email cannot be empty")
    String email;

    String phone;

    String address;

}
