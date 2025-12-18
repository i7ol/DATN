package com.datn.shopobject.dto.request;

public record UpdateOrderStatusRequest(
        Long orderId,
        String orderStatus
) {}