package com.datn.shoppayment.dto.request;

public record UpdateOrderPaymentRequest(Long orderId, String paymentStatus) {}
