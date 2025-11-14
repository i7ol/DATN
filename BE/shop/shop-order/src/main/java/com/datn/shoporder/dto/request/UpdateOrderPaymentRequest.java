package com.datn.shoporder.dto.request;

public record UpdateOrderPaymentRequest(Long orderId, String paymentStatus) {
}
