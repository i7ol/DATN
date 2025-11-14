package com.datn.shoppayment.dto.response;

import com.datn.shoppayment.enums.PaymentStatus;

public record PaymentResponse(Long paymentId, Long orderId, PaymentStatus status) {}
