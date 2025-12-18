
package com.datn.shopobject.dto.response;

import com.datn.shopdatabase.entity.PaymentEntity;
import com.datn.shopdatabase.enums.PaymentMethod;
import com.datn.shopdatabase.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private Long userId;
    private PaymentStatus status;
    private BigDecimal amount;
    private PaymentMethod method;
    private String transactionId;
    private LocalDateTime paidAt;
    private LocalDateTime refundedAt;
    private LocalDateTime cancelledAt;
    private String refundReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PaymentResponse from(PaymentEntity p) {
        return PaymentResponse.builder()
                .id(p.getId())
                .orderId(p.getOrderId())
                .userId(p.getUserId())
                .status(p.getStatus())
                .amount(p.getAmount())
                .method(p.getMethod())
                .transactionId(p.getTransactionId())
                .paidAt(p.getPaidAt())
                .refundedAt(p.getRefundedAt())
                .cancelledAt(p.getCancelledAt())
                .refundReason(p.getRefundReason())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}