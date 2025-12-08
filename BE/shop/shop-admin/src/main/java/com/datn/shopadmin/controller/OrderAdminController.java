package com.datn.shopadmin.controller;

import com.datn.shopdatabase.exception.AppException;
import com.datn.shopdatabase.exception.ErrorCode;
import com.datn.shopobject.dto.request.CreateOrderRequest;
import com.datn.shopobject.dto.request.PaymentUpdateRequest;
import com.datn.shopobject.dto.request.StatusUpdateRequest;

import com.datn.shopdatabase.entity.OrderEntity;
import com.datn.shopdatabase.enums.OrderStatus;
import com.datn.shopdatabase.enums.PaymentStatus;
import com.datn.shoporder.service.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class OrderAdminController {

    private final OrderService orderService;

    // Tạo đơn hàng
    @PostMapping
    public OrderEntity createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return orderService.createOrder(request);
    }

    // Lấy đơn hàng theo user
    @GetMapping("/user/{userId}")
    public List<OrderEntity> getOrdersByUser(@PathVariable Long userId) {
        return orderService.getOrdersByUserId(userId);
    }

    // Admin xem tất cả đơn hàng
    @GetMapping
    public List<OrderEntity> getAllOrders() {
        return orderService.getAllOrders();
    }

    // Lất 1 đơn
    @GetMapping("/{orderId}")
    public OrderEntity getOrder(@PathVariable Long orderId) {
        return orderService.getOrderById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
    }

    // Cập nhật trạng thái đơn hàng
    @PutMapping("/{id}/status")
    public OrderEntity updateOrderStatus(
            @PathVariable Long id,
            @RequestBody StatusUpdateRequest request
    ) {
        try {
            OrderStatus status = OrderStatus.valueOf(request.getStatus().trim().toUpperCase());
            return orderService.updateStatus(id, status);
        } catch (IllegalArgumentException e) {
            String valid = Arrays.stream(OrderStatus.values()).map(Enum::name).collect(Collectors.joining(", "));
            throw new AppException(ErrorCode.INVALID_REQUEST, "Status không hợp lệ: " + valid);
        }
    }

    // Cập nhật trạng thái thanh toán
    @PutMapping("/{id}/payment")
    public OrderEntity updatePayment(@PathVariable Long id,
                                     @RequestBody PaymentUpdateRequest request) {

        try {
            PaymentStatus ps = PaymentStatus.valueOf(request.getPaymentStatus().trim().toUpperCase());
            return orderService.updatePaymentStatus(id, ps);
        } catch (IllegalArgumentException e) {
            String valid = Arrays.stream(PaymentStatus.values()).map(Enum::name).collect(Collectors.joining(", "));
            throw new AppException(ErrorCode.INVALID_REQUEST, "PaymentStatus không hợp lệ: " + valid);
        }
    }

    // Đơn guest
    @GetMapping("/guests")
    public List<OrderEntity> getGuestOrders() {
        return orderService.getGuestOrders();
    }
}

