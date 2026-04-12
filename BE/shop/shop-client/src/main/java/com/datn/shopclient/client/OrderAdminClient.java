package com.datn.shopclient.client;

import com.datn.shopdatabase.enums.OrderStatus;
import com.datn.shopdatabase.enums.PaymentStatus;
import com.datn.shopobject.dto.response.OrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
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

    @GetMapping(value = "/api/admin/orders",produces = MediaType.APPLICATION_JSON_VALUE)
    Page<OrderResponse> getAll(Pageable pageable);

    @GetMapping(value = "/api/admin/orders/{orderId}",produces = MediaType.APPLICATION_JSON_VALUE)
    OrderResponse getOrder(@PathVariable Long orderId);

    @PutMapping(value = "/api/admin/orders/{orderId}/status",produces = MediaType.APPLICATION_JSON_VALUE)
    OrderResponse updateStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status
    );

    @PutMapping(value = "/api/admin/orders/{orderId}/payment-status",produces = MediaType.APPLICATION_JSON_VALUE)
    OrderResponse updatePaymentStatus(
            @PathVariable Long orderId,
            @RequestParam PaymentStatus paymentStatus
    );
}

