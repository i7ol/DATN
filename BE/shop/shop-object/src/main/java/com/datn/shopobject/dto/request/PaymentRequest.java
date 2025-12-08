package com.datn.shopobject.dto.request;

import java.math.BigDecimal;

public record PaymentRequest(
        Long orderId,
        BigDecimal amount,
        String method
) {}
