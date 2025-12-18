package com.datn.shopauth.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private boolean authenticated;
    private String username;
    private List<String> roles;
    private List<String> permissions;
}