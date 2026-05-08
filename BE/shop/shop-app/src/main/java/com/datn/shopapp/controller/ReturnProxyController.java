package com.datn.shopapp.controller;

import com.datn.shopclient.client.ReturnUserClient;
import com.datn.shopdatabase.exception.AppException;
import com.datn.shopdatabase.exception.ErrorCode;
import com.datn.shopobject.dto.request.CreateReturnRequest;
import com.datn.shopobject.dto.response.ReturnResponse;
import com.datn.shopobject.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/returns")
@RequiredArgsConstructor
@Slf4j
public class ReturnProxyController {

    private final ReturnUserClient returnUserClient;

    // Tạo yêu cầu đổi trả / bảo hành
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ReturnResponse createReturn(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody CreateReturnRequest request) {

        log.info("=== CREATE RETURN REQUEST RECEIVED ===");
        log.info("OrderId: {}, ReturnType: {}, Items: {}",
                request.getOrderId(), request.getReturnType(), request.getItems());
        // Truyền thông tin user nếu có
        if (principal != null) {
            // Frontend có thể không cần gửi guestId khi user đã login
            request.setGuestId(null);
            request.setGuestPhone(null);
        }
        log.info("Creating return for orderId: {}", request.getOrderId());
        return returnUserClient.createReturn(request);
    }

    // Lấy danh sách yêu cầu của tôi
    @GetMapping(value = "/my-returns", produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<ReturnResponse> getMyReturns(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        if (principal == null) {
            log.error("Principal is null when accessing my-returns");
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return returnUserClient.getMyReturns(page, size);
    }

    // Chi tiết yêu cầu đổi trả
    @GetMapping(value = "/{returnId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ReturnResponse getReturnDetail(
            @PathVariable Long returnId,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return returnUserClient.getReturnById(returnId);
    }
}