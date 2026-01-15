package com.datn.shopadmin.controller;

import com.datn.shopclient.client.OrderAdminClient;
import com.datn.shopdatabase.enums.PaymentStatus;
import com.datn.shopdatabase.exception.AppException;
import com.datn.shopdatabase.exception.ErrorCode;
import com.datn.shopobject.dto.request.CreateOrderRequest;
import com.datn.shopobject.dto.request.PaymentUpdateRequest;
import com.datn.shopobject.dto.request.StatusUpdateRequest;
import com.datn.shopobject.dto.response.OrderResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class OrderAdminController {

    private final OrderAdminClient orderClient;

    @PostMapping
    public OrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {
        // nếu cần endpoint riêng thì tạo thêm ở order-service
        throw new AppException(
                ErrorCode.INVALID_REQUEST,
                "Admin createOrder chưa được expose ở order-service"
        );
    }

    /**
     * Admin xem chi tiết đơn hàng
     */
    @GetMapping("/{orderId}")
    public OrderResponse getOrder(@PathVariable Long orderId) {
        return orderClient.getOrder(orderId);
    }

    /**
     * Admin cập nhật trạng thái thanh toán
     */
    @PutMapping("/{orderId}/payment-status")
    public OrderResponse updatePaymentStatus(
            @PathVariable Long orderId,
            @RequestBody PaymentUpdateRequest request
    ) {
        try {
            PaymentStatus status =
                    PaymentStatus.valueOf(
                            request.getPaymentStatus().trim().toUpperCase()
                    );

            return orderClient.updatePaymentStatus(
                    orderId,
                    status
            );

        } catch (IllegalArgumentException e) {
            String valid = Arrays.stream(PaymentStatus.values())
                    .map(Enum::name)
                    .collect(Collectors.joining(", "));
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "PaymentStatus không hợp lệ. Hợp lệ: " + valid
            );
        }
    }


    /**
     * Admin cập nhật trạng thái đơn hàng
     * (cần endpoint riêng bên order-service)
     */
    @PutMapping("/{orderId}/status")
    public OrderResponse updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody StatusUpdateRequest request
    ) {
        throw new AppException(
                ErrorCode.INVALID_REQUEST,
                "Admin updateStatus cần endpoint riêng trong order-service"
        );
    }

}
