package com.datn.shopobject.dto.response;

import com.datn.shopdatabase.entity.PaymentEntity;
import com.datn.shopdatabase.enums.PaymentMethod;
import com.datn.shopdatabase.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    // Thêm các trường cho VNPay
    private String paymentUrl;
    private String qrCodeUrl;

    // Setter cho các trường VNPay
    public void setPaymentUrl(String paymentUrl) {
        this.paymentUrl = paymentUrl;
    }

    public void setQrCodeUrl(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

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