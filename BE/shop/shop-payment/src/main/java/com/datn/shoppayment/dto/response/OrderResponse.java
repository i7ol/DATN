package com.datn.shoppayment.dto.response;

import java.math.BigDecimal;

public record OrderResponse(Long id, BigDecimal totalPrice, String paymentStatus) {}
