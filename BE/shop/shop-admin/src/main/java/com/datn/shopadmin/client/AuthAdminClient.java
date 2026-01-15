package com.datn.shopadmin.client;

import com.datn.shopobject.dto.response.UserInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "auth-admin-service",
        url = "${feign.auth-service-url:http://localhost:8082}"
)
public interface AuthAdminClient {

    @GetMapping("/api/auth/validate")
    boolean validateToken(
            @RequestHeader("Authorization") String token
    );

    @GetMapping("/api/auth/me")
    UserInfoResponse getCurrentUser(
            @RequestHeader("Authorization") String token
    );
}

