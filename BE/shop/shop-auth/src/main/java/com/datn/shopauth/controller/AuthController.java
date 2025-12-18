package com.datn.shopauth.controller;

import com.datn.shopauth.dto.request.LoginRequest;
import com.datn.shopauth.dto.request.RefreshTokenRequest;
import com.datn.shopauth.dto.response.AuthResponse;
import com.datn.shopobject.dto.request.RegisterRequest;
import com.datn.shopobject.dto.response.RegisterResponse;
import com.datn.shopobject.dto.response.UserInfoResponse;
import com.datn.shopauth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        log.info("Register endpoint called for username: {}", request.getUsername());
        RegisterResponse response = authService.register(request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/check-username")
    public ResponseEntity<Boolean> checkUsernameAvailability(
            @RequestParam String username) {
        boolean isAvailable = authService.checkUsernameAvailability(username);
        return ResponseEntity.ok(isAvailable);
    }

    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmailAvailability(
            @RequestParam String email) {
        boolean isAvailable = authService.checkEmailAvailability(email);
        return ResponseEntity.ok(isAvailable);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestHeader(value = "Refresh-Token", required = false) String refreshToken) {

        String accessToken = authorizationHeader.replace("Bearer ", "");
        authService.logout(accessToken, refreshToken);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/validate")
    public ResponseEntity<Boolean> validateToken(
            @RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");
        boolean isValid = authService.validateToken(token);
        return ResponseEntity.ok(isValid);
    }

    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getCurrentUser(
            @RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");
        UserInfoResponse userInfo = authService.getCurrentUser(token);
        return ResponseEntity.ok(userInfo);
    }
    @GetMapping("/check-permission")
    public ResponseEntity<Boolean> checkPermission(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam String permission) {
        String token = authorizationHeader.replace("Bearer ", "");
        boolean hasPermission = authService.checkPermission(token, permission);
        return ResponseEntity.ok(hasPermission);
    }
}