package com.datn.shoporder.controller;

import com.datn.shopdatabase.enums.PaymentStatus;
import com.datn.shopobject.dto.response.OrderResponse;
import com.datn.shoporder.mapper.OrderMapper;
import com.datn.shoporder.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/internal/orders")
@RequiredArgsConstructor
public class OrderInternalController {

    private final OrderService orderService;

    @GetMapping("/{orderId}")
    public OrderResponse getOrder(@PathVariable Long orderId) {
        return OrderMapper.toResponse(orderService.getOrder(orderId));
    }

    @PutMapping("/{orderId}/payment-status")
    public OrderResponse updatePayment(
            @PathVariable Long orderId,
            @RequestParam PaymentStatus paymentStatus
    ) {
        return OrderMapper.toResponse(
                orderService.updatePaymentStatus(orderId, paymentStatus)
        );
    }
}


