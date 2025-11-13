//package com.datn.shoporder.controller;
//
//import com.datn.shopcore.exception.AppException;
//import com.datn.shopcore.exception.ErrorCode;
//import com.datn.shoporder.dto.request.CreateOrderRequest;
//import com.datn.shoporder.dto.request.PaymentUpdateRequest;
//import com.datn.shoporder.dto.request.StatusUpdateRequest;
//import com.datn.shoporder.entity.Order;
//import com.datn.shoporder.enums.OrderStatus;
//import com.datn.shoporder.enums.PaymentStatus;
//import com.datn.shoporder.service.OrderService;
//import jakarta.validation.Valid;
//import lombok.AllArgsConstructor;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/orders")
//@AllArgsConstructor
//public class OrderController {
//
//    private final OrderService orderService;
//
//    // Tạo đơn hàng (userId lấy từ token hoặc DTO)
//    @PostMapping
//    public Order createOrder(@Valid @RequestBody CreateOrderRequest request) {
//        return orderService.createOrder(request);
//    }
//
//    // Lấy đơn hàng theo userId
//    @GetMapping("/user/{userId}")
//    public List<Order> getOrdersByUser(@PathVariable("userId") Long userId) {
//        return orderService.getOrdersByUserById(userId);
//    }
//
//    // Lấy tất cả đơn hàng (có thể dùng cho admin)
//    @GetMapping
//    public List<Order> getAllOrders() {
//        return orderService.getAllOrders();
//    }
//
//// Ví dụ cải tiến trong OrderController.java
//
//    @PutMapping("/{id}/status")
//    public Order updateOrderStatus(@PathVariable Long id,
//                                   @RequestBody StatusUpdateRequest request) {
//        OrderStatus orderStatus;
//        try {
//            orderStatus = OrderStatus.valueOf(request.getStatus().trim().toUpperCase());
//        } catch (IllegalArgumentException e) {
//            // Cung cấp thông tin chi tiết hơn về lỗi:
//            String validStatuses = java.util.Arrays.stream(OrderStatus.values())
//                    .map(Enum::name)
//                    .collect(java.util.stream.Collectors.joining(", "));
//            throw new AppException(ErrorCode.INVALID_REQUEST);
//        }
//        return orderService.updateStatus(id, orderStatus);
//    }
//
//    // Cập nhật trạng thái thanh toán bằng JSON body
//    @PutMapping("/{id}/payment")
//    public Order updatePaymentStatus(@PathVariable Long id,
//                                     @RequestBody PaymentUpdateRequest request) {
//        PaymentStatus ps;
//        try {
//            ps = PaymentStatus.valueOf(request.getPaymentStatus().trim().toUpperCase());
//        } catch (IllegalArgumentException e) {
//            throw new AppException(ErrorCode.INVALID_REQUEST);
//        }
//        return orderService.updatePaymentStatus(id, ps);
//    }
//
//
//
//
//    // Lấy tất cả đơn hàng của guest
//    @GetMapping("/guests")
//    public List<Order> getGuestOrders() {
//        return orderService.getGuestOrders();
//    }
//}
//
package com.datn.shoporder.controller;

import com.datn.shopcore.exception.AppException;
import com.datn.shopcore.exception.ErrorCode;
import com.datn.shoporder.dto.request.CreateOrderRequest;
import com.datn.shoporder.dto.request.PaymentUpdateRequest;
import com.datn.shoporder.dto.request.StatusUpdateRequest;
import com.datn.shoporder.entity.Order;
import com.datn.shoporder.enums.OrderStatus;
import com.datn.shoporder.enums.PaymentStatus;
import com.datn.shoporder.service.OrderService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Arrays;

@RestController
@RequestMapping("/api/orders")
@AllArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // Tạo đơn hàng (userId lấy từ token hoặc DTO)
    @PostMapping
    public Order createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return orderService.createOrder(request);
    }

    // Lấy đơn hàng theo userId
    @GetMapping("/user/{userId}")
    public List<Order> getOrdersByUser(@PathVariable("userId") Long userId) {
        return orderService.getOrdersByUserById(userId);
    }

    // Lấy tất cả đơn hàng (có thể dùng cho admin)
    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @PutMapping("/{id}/status")
    public Order updateOrderStatus(@PathVariable("id") Long id,
                                   @RequestBody StatusUpdateRequest request) {
        OrderStatus orderStatus;
        try {
            orderStatus = OrderStatus.valueOf(request.getStatus().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            String validStatuses = Arrays.stream(OrderStatus.values())
                    .map(Enum::name)
                    .collect(Collectors.joining(", "));

            // SỬ DỤNG: AppException với thông báo tùy chỉnh để liệt kê các giá trị hợp lệ
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "Giá trị trạng thái đơn hàng không hợp lệ: '" + request.getStatus() + "'. Các giá trị HỢP LỆ là: " + validStatuses
            );
        }
        return orderService.updateStatus(id, orderStatus);
    }

    // Cập nhật trạng thái thanh toán bằng JSON body
    @PutMapping("/{id}/payment")
    public Order updatePaymentStatus(@PathVariable("id") Long id,
                                     @RequestBody PaymentUpdateRequest request) {
        PaymentStatus ps;
        try {
            ps = PaymentStatus.valueOf(request.getPaymentStatus().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            String validPaymentStatuses = Arrays.stream(PaymentStatus.values())
                    .map(Enum::name)
                    .collect(Collectors.joining(", "));

            // SỬ DỤNG: AppException với thông báo tùy chỉnh để liệt kê các giá trị hợp lệ
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "Giá trị trạng thái thanh toán không hợp lệ: '" + request.getPaymentStatus() + "'. Các giá trị HỢP LỆ là: " + validPaymentStatuses
            );
        }
        return orderService.updatePaymentStatus(id, ps);
    }

    // Lấy tất cả đơn hàng của guest
    @GetMapping("/guests")
    public List<Order> getGuestOrders() {
        return orderService.getGuestOrders();
    }
}