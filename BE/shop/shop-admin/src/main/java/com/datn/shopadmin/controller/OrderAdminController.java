package com.datn.shopadmin.controller;

import com.datn.shopclient.client.OrderAdminClient;

import com.datn.shopobject.dto.request.PaymentUpdateRequest;
import com.datn.shopobject.dto.request.StatusUpdateRequest;
import com.datn.shopobject.dto.response.OrderResponse;
import com.datn.shopobject.dto.response.PageResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class OrderAdminController {

    private final OrderAdminClient orderClient;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public PageResponse<OrderResponse> getAll(Pageable pageable) {

        Page<OrderResponse> page = orderClient.getAll(pageable);

        return PageResponse.<OrderResponse>builder()
                .data(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }



    /**
     * Admin xem chi tiết đơn hàng
     */
    @GetMapping(value = "/{orderId}",produces = MediaType.APPLICATION_JSON_VALUE)
    public OrderResponse getOrder(@PathVariable Long orderId) {
        return orderClient.getOrder(orderId);
    }

    /**
     * Admin cập nhật trạng thái thanh toán
     */
    @PutMapping(value = "/{orderId}/payment-status",produces = MediaType.APPLICATION_JSON_VALUE)
    public OrderResponse updatePaymentStatus(
            @PathVariable Long orderId,
            @RequestBody PaymentUpdateRequest request
    ) {
        return orderClient.updatePaymentStatus(
                orderId,
                request.getPaymentStatus()
        );
    }

    /**
     * Admin cập nhật trạng thái đơn hàng
     * (cần endpoint riêng bên order-service)
     */
    @PutMapping(value = "/{orderId}/status",produces = MediaType.APPLICATION_JSON_VALUE)
    public OrderResponse updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody StatusUpdateRequest request
    ) {
        return orderClient.updateStatus(
                orderId,
                request.getStatus()
        );
    }



}
