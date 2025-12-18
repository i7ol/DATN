package com.datn.shopobject.client;


import com.datn.shopobject.dto.response.UserInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "auth-service",
        url = "${feign.auth-service-url:http://localhost:8082}",
        fallback = AuthClientFallback.class  // Optional: for circuit breaker
)
public interface AuthClient {

    @GetMapping("/api/auth/validate")
    boolean validateToken(@RequestHeader("Authorization") String token);

    @GetMapping("/api/auth/me")
    UserInfoResponse getCurrentUser(@RequestHeader("Authorization") String token);

    // Thêm các endpoints khác nếu cần
    @GetMapping("/api/auth/check-permission")
    boolean checkPermission(
            @RequestHeader("Authorization") String token,
            @RequestParam String permission);
}
