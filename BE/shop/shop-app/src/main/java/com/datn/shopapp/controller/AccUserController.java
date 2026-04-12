package com.datn.shopapp.controller;

import com.datn.shopapp.config.SecurityUtils;
import com.datn.shopclient.client.OrderUserClient;
import com.datn.shopdatabase.dto.ApiResponse;
import com.datn.shopdatabase.exception.AppException;
import com.datn.shopdatabase.exception.ErrorCode;
import com.datn.shopobject.dto.request.UserUpdateRequest;
import com.datn.shopobject.dto.response.OrderResponse;
import com.datn.shopobject.dto.response.UserResponse;
import com.datn.shopuser.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/account")
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AccUserController {

    final UserService userService;
    final OrderUserClient orderUserClient;

    @GetMapping(value = "/me",produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<UserResponse> me() {
        Long userId = SecurityUtils.getCurrentUserId();

        if (userId == null) {
            log.error("UserId is null in /me endpoint - Authentication failed");
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        log.info("Getting user info for userId: {}", userId);

        UserResponse user = userService.getUserById(userId);

        return ApiResponse.<UserResponse>builder()
                .result(user)
                .build();
    }

    @PutMapping(value = "/me",produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<UserResponse> updateMe(@Valid @RequestBody UserUpdateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        if (userId == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        UserResponse updatedUser = userService.updateMe(userId, request);

        return ApiResponse.<UserResponse>builder()
                .result(updatedUser)
                .build();
    }

    @GetMapping(value = "/my-orders",produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<Page<OrderResponse>> myOrders(Pageable pageable) {
        Page<OrderResponse> orders = orderUserClient.myOrders(
                pageable.getPageNumber(),
                pageable.getPageSize()
        );

        return ApiResponse.<Page<OrderResponse>>builder()
                .result(orders)
                .build();
    }
}
