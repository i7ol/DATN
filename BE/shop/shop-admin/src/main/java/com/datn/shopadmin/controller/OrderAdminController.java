//package com.datn.shopadmin.controller;
//
//import com.datn.shopdatabase.exception.AppException;
//import com.datn.shopdatabase.exception.ErrorCode;
//import com.datn.shopobject.dto.request.CreateOrderRequest;
//import com.datn.shopobject.dto.request.PaymentUpdateRequest;
//import com.datn.shopobject.dto.request.StatusUpdateRequest;
//
//import com.datn.shopdatabase.entity.OrderEntity;
//import com.datn.shopdatabase.enums.OrderStatus;
//import com.datn.shopdatabase.enums.PaymentStatus;
//import com.datn.shoporder.service.OrderService;
//
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@RestController
//@RequestMapping("/api/admin/orders")
//@RequiredArgsConstructor
//public class OrderAdminController {
//
//    private final OrderService orderService;
//
//    // Tạo đơn hàng
//    @PostMapping
//    public OrderEntity createOrder(@Valid @RequestBody CreateOrderRequest request) {
//        return orderService.createOrder(request);
//    }
//
//    // Lấy đơn hàng theo user
//    @GetMapping("/user/{userId}")
//    public List<OrderEntity> getOrdersByUser(@PathVariable Long userId) {
//        return orderService.getOrdersByUserId(userId);
//    }
//
//    // Admin xem tất cả đơn hàng
//    @GetMapping
//    public List<OrderEntity> getAllOrders() {
//        return orderService.getAllOrders();
//    }
//
//    // Lất 1 đơn
//    @GetMapping("/{orderId}")
//    public OrderEntity getOrder(@PathVariable Long orderId) {
//        return orderService.getOrderById(orderId)
//                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
//    }
//
//    // Cập nhật trạng thái đơn hàng
//    @PutMapping("/{id}/status")
//    public OrderEntity updateOrderStatus(
//            @PathVariable Long id,
//            @RequestBody StatusUpdateRequest request
//    ) {
//        try {
//            OrderStatus status = OrderStatus.valueOf(request.getStatus().trim().toUpperCase());
//            return orderService.updateStatus(id, status);
//        } catch (IllegalArgumentException e) {
//            String valid = Arrays.stream(OrderStatus.values()).map(Enum::name).collect(Collectors.joining(", "));
//            throw new AppException(ErrorCode.INVALID_REQUEST, "Status không hợp lệ: " + valid);
//        }
//    }
//
//    // Cập nhật trạng thái thanh toán
//    @PutMapping("/{id}/payment")
//    public OrderEntity updatePayment(@PathVariable Long id,
//                                     @RequestBody PaymentUpdateRequest request) {
//
//        try {
//            PaymentStatus ps = PaymentStatus.valueOf(request.getPaymentStatus().trim().toUpperCase());
//            return orderService.updatePaymentStatus(id, ps);
//        } catch (IllegalArgumentException e) {
//            String valid = Arrays.stream(PaymentStatus.values()).map(Enum::name).collect(Collectors.joining(", "));
//            throw new AppException(ErrorCode.INVALID_REQUEST, "PaymentStatus không hợp lệ: " + valid);
//        }
//    }
//
//    // Đơn guest
//    @GetMapping("/guests")
//    public List<OrderEntity> getGuestOrders() {
//        return orderService.getGuestOrders();
//    }
//
//    // OrderAdminController.java - THÊM phương thức này
//    @PutMapping("/{id}/payment-status")
//    public OrderEntity updatePaymentStatus(@PathVariable Long id,
//                                           @RequestBody PaymentUpdateRequest request) {
//        try {
//            PaymentStatus ps = PaymentStatus.valueOf(request.getPaymentStatus().trim().toUpperCase());
//            return orderService.updatePaymentStatus(id, ps);
//        } catch (IllegalArgumentException e) {
//            String valid = Arrays.stream(PaymentStatus.values()).map(Enum::name).collect(Collectors.joining(", "));
//            throw new AppException(ErrorCode.INVALID_REQUEST, "PaymentStatus không hợp lệ: " + valid);
//        }
//    }
//}
//
// OrderAdminController.java - Đặt trong shop-admin
package com.datn.shopadmin.controller;

import com.datn.shopdatabase.entity.OrderEntity;
import com.datn.shopdatabase.enums.OrderStatus;
import com.datn.shopdatabase.enums.PaymentStatus;
import com.datn.shopdatabase.exception.AppException;
import com.datn.shopdatabase.exception.ErrorCode;
import com.datn.shopobject.dto.request.CreateOrderRequest;
import com.datn.shopobject.dto.request.PaymentUpdateRequest;
import com.datn.shopobject.dto.request.StatusUpdateRequest;
import com.datn.shopobject.dto.response.OrderResponse;
import com.datn.shoporder.mapper.OrderMapper;
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

    // =========================
    // ADMIN ORDER MANAGEMENT
    // =========================

    /**
     * Admin tạo đơn hàng thủ công
     */
    @PostMapping
    public OrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderEntity order = orderService.createOrder(request);
        return OrderMapper.toResponse(order);
    }

    /**
     * Admin xem tất cả đơn hàng
     */
    @GetMapping
    public List<OrderResponse> getAllOrders() {
        List<OrderEntity> orders = orderService.getAllOrders();
        return orders.stream()
                .map(OrderMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Admin xem chi tiết đơn hàng
     */
    @GetMapping("/{orderId}")
    public OrderResponse getOrder(@PathVariable Long orderId) {
        OrderEntity order = orderService.getOrderById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        return OrderMapper.toResponse(order);
    }

    /**
     * Admin xem đơn hàng theo user
     */
    @GetMapping("/user/{userId}")
    public List<OrderResponse> getOrdersByUser(@PathVariable Long userId) {
        List<OrderEntity> orders = orderService.getOrdersByUserId(userId);
        return orders.stream()
                .map(OrderMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Admin cập nhật trạng thái đơn hàng
     */
    @PutMapping("/{orderId}/status")
    public OrderResponse updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody StatusUpdateRequest request
    ) {
        try {
            OrderStatus status = OrderStatus.valueOf(request.getStatus().trim().toUpperCase());
            OrderEntity updated = orderService.updateStatus(orderId, status);
            return OrderMapper.toResponse(updated);
        } catch (IllegalArgumentException e) {
            String valid = Arrays.stream(OrderStatus.values())
                    .map(Enum::name)
                    .collect(Collectors.joining(", "));
            throw new AppException(ErrorCode.INVALID_REQUEST, "Status không hợp lệ. Hợp lệ: " + valid);
        }
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
            PaymentStatus status = PaymentStatus.valueOf(request.getPaymentStatus().trim().toUpperCase());
            OrderEntity updated = orderService.updatePaymentStatus(orderId, status);
            return OrderMapper.toResponse(updated);
        } catch (IllegalArgumentException e) {
            String valid = Arrays.stream(PaymentStatus.values())
                    .map(Enum::name)
                    .collect(Collectors.joining(", "));
            throw new AppException(ErrorCode.INVALID_REQUEST, "PaymentStatus không hợp lệ. Hợp lệ: " + valid);
        }
    }

    /**
     * Admin xem đơn hàng guest
     */
    @GetMapping("/guests")
    public List<OrderResponse> getGuestOrders() {
        List<OrderEntity> orders = orderService.getGuestOrders();
        return orders.stream()
                .map(OrderMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Admin xem đơn hàng theo trạng thái
     */
    @GetMapping("/status/{status}")
    public List<OrderResponse> getOrdersByStatus(@PathVariable String status) {
        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            List<OrderEntity> orders = orderService.getOrdersByStatus(orderStatus);
            return orders.stream()
                    .map(OrderMapper::toResponse)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Status không hợp lệ");
        }
    }

    /**
     * Admin xem đơn hàng theo trạng thái thanh toán
     */
    @GetMapping("/payment-status/{paymentStatus}")
    public List<OrderResponse> getOrdersByPaymentStatus(@PathVariable String paymentStatus) {
        try {
            PaymentStatus status = PaymentStatus.valueOf(paymentStatus.toUpperCase());
            List<OrderEntity> orders = orderService.getOrdersByPaymentStatus(status);
            return orders.stream()
                    .map(OrderMapper::toResponse)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "PaymentStatus không hợp lệ");
        }
    }
}