package com.datn.shopclient.client;

import com.datn.shopdatabase.enums.OrderStatus;
import com.datn.shopdatabase.enums.PaymentStatus;
import com.datn.shopobject.dto.response.OrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
        name = "order-admin",
        url = "${order.service.url}"
)
public interface OrderAdminClient {

    @GetMapping("/api/admin/orders")
    List<OrderResponse> getAll();

    @GetMapping("/api/admin/orders/{orderId}")
    OrderResponse getOrder(@PathVariable Long orderId);

    @PutMapping("/api/admin/orders/{orderId}/status")
    OrderResponse updateStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status
    );

    @PutMapping("/api/admin/orders/{orderId}/payment-status")
    OrderResponse updatePaymentStatus(
            @PathVariable Long orderId,
            @RequestParam PaymentStatus paymentStatus
    );
}

