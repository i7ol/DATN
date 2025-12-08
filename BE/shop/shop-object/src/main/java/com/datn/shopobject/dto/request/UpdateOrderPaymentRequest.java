package com.datn.shopobject.dto.request;

public record UpdateOrderPaymentRequest(Long orderId, String paymentStatus) {
}
