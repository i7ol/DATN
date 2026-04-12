package com.datn.shopclient.client;

import com.datn.shopdatabase.enums.PaymentStatus;
import com.datn.shopobject.dto.response.OrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "order-internal",
        url = "${order.service.url}"
)
public interface OrderInternalClient {

    @GetMapping(value = "/api/internal/orders/{orderId}",produces = MediaType.APPLICATION_JSON_VALUE)
    OrderResponse getOrder(@PathVariable("orderId") Long orderId);

    @PutMapping(value = "/api/internal/orders/{orderId}/payment-status",produces = MediaType.APPLICATION_JSON_VALUE)
    OrderResponse updatePaymentStatus(
            @PathVariable("orderId") Long orderId,
            @RequestParam("paymentStatus") PaymentStatus paymentStatus
    );

}