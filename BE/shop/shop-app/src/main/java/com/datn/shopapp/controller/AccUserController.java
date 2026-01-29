package com.datn.shopapp.controller;

import com.datn.shopapp.config.SecurityUtils;
import com.datn.shopdatabase.dto.ApiResponse;
import com.datn.shopobject.dto.response.UserResponse;
import com.datn.shopuser.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/account")
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class AccUserController {

    final UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserResponse> me() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.<UserResponse>builder()
                .result(userService.getUserById(userId))
                .build();
    }



}
