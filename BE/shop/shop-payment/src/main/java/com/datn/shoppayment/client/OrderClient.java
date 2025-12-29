package com.datn.shoppayment.client;

import com.datn.shopobject.dto.response.OrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "order-service", url = "${order.service.url}")
public interface OrderClient {
    @GetMapping("/api/internal/orders/{orderId}")
    OrderResponse getOrder(@PathVariable Long orderId);

    @PutMapping("/api/internal/orders/{orderId}/payment-status")
    OrderResponse updateOrderPayment(
            @PathVariable Long orderId,
            @RequestParam String paymentStatus);
}