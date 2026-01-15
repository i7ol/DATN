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
    private String guestId;

    private PaymentStatus status;
    private BigDecimal amount;
    private PaymentMethod method;

    private String transactionId;
    private LocalDateTime paidAt;

    private String paymentUrl;

    public static PaymentResponse from(PaymentEntity p) {
        return PaymentResponse.builder()
                .id(p.getId())
                .orderId(p.getOrderId())
                .userId(p.getUserId())
                .guestId(p.getGuestId())
                .status(p.getStatus())
                .amount(p.getAmount())
                .method(p.getMethod())
                .transactionId(p.getTransactionId())
                .paidAt(p.getPaidAt())
                .build();
    }
}
