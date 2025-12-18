package com.datn.shopobject.dto.response;

import com.datn.shopdatabase.enums.OrderStatus;
import com.datn.shopdatabase.enums.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private Long id;
    private Long userId;
    private String guestName;
    private String guestEmail;
    private String guestPhone;

    private String shippingAddress;
    private String billingAddress;

    private OrderStatus status;
    private PaymentStatus paymentStatus;

    private BigDecimal totalPrice;
    private BigDecimal shippingFee;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;

    private String paymentMethod;
    private String shippingMethod;

    private List<OrderItemResponse> items;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime paymentDate;
    private LocalDateTime estimatedDeliveryDate;
    private LocalDateTime actualDeliveryDate;
}