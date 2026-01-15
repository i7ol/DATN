package com.datn.shoporder.client;

import com.datn.shopobject.dto.response.UserInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "auth-order-service",
        url = "${feign.auth-service-url:http://localhost:8082}"
)
public interface AuthOrderClient {

    @GetMapping("/api/auth/validate")
    boolean validateToken(
            @RequestHeader("Authorization") String token
    );

    @GetMapping("/api/auth/me")
    UserInfoResponse getCurrentUser(
            @RequestHeader("Authorization") String token
    );

    @GetMapping("/api/auth/check-permission")
    boolean checkPermission(
            @RequestHeader("Authorization") String token,
            @RequestParam("permission") String permission
    );
}