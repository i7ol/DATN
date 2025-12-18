package com.datn.shopdatabase.entity;

import com.datn.shopdatabase.enums.OrderStatus;
import com.datn.shopdatabase.enums.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    // Guest info
    private String guestName;
    private String guestEmail;
    private String guestPhone;

    // Shipping address details
    private String shippingAddress;
    private String shippingProvince;
    private String shippingDistrict;
    private String shippingWard;
    private String shippingNote;

    // Billing address (nếu khác shipping)
    private String billingAddress;
    private String billingProvince;
    private String billingDistrict;
    private String billingWard;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<OrderItemEntity> items = new ArrayList<>();

    private BigDecimal totalPrice;
    private BigDecimal shippingFee = BigDecimal.ZERO;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private BigDecimal finalAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    // Thông tin thanh toán
    private String paymentMethod;
    private String paymentTransactionId;
    private Instant paymentDate; // Đổi từ LocalDateTime sang Instant

    // Thông tin giao hàng
    private String shippingMethod;
    private Instant estimatedDeliveryDate; // Đổi từ LocalDateTime sang Instant
    private Instant actualDeliveryDate; // Đổi từ LocalDateTime sang Instant

    // Helper methods
    public void calculateFinalAmount() {
        BigDecimal subtotal = this.totalPrice != null ? this.totalPrice : BigDecimal.ZERO;
        BigDecimal shipping = this.shippingFee != null ? this.shippingFee : BigDecimal.ZERO;
        BigDecimal discount = this.discountAmount != null ? this.discountAmount : BigDecimal.ZERO;

        this.finalAmount = subtotal.add(shipping).subtract(discount);
    }

    public void addItem(OrderItemEntity item) {
        item.setOrder(this);
        this.items.add(item);
    }
}