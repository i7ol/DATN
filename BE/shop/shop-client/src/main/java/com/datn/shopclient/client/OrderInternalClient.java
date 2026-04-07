package com.datn.shopclient.client;

import com.datn.shopdatabase.enums.PaymentStatus;
import com.datn.shopobject.dto.response.OrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "order-internal",
        url = "${order.service.url}"
)
public interface OrderInternalClient {

    @GetMapping("/api/internal/orders/{orderId}")
    OrderResponse getOrder(@PathVariable("orderId") Long orderId);

    @PutMapping("/api/internal/orders/{orderId}/payment-status")
    OrderResponse updatePaymentStatus(
            @PathVariable("orderId") Long orderId,
            @RequestParam("paymentStatus") PaymentStatus paymentStatus
    );

}