package com.datn.shopdatabase.entity;

import com.datn.shopdatabase.enums.OrderStatus;
import com.datn.shopdatabase.enums.PaymentMethod;
import com.datn.shopdatabase.enums.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
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

    @Column(name = "guest_id", length = 100)
    private String guestId;

    // Guest info
    private String guestName;
    private String guestEmail;
    private String guestPhone;

    // Shipping address details
    private String shippingAddress;
    private Integer  shippingProvince;
    private Integer  shippingDistrict;
    private Integer  shippingWard;
    private String shippingNote;

    // Billing address (nếu khác shipping)
    private String billingAddress;
    private Integer billingProvince;
    private Integer billingDistrict;
    private Integer billingWard;

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
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
    private String paymentTransactionId;
    private Instant paymentDate;

    // Thông tin giao hàng
    private String shippingMethod;
    private LocalDateTime  estimatedDeliveryDate;
    private LocalDateTime  actualDeliveryDate;

    @Column(name = "tracking_code", unique = true)
    private String trackingCode;

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
    public void setItems(List<OrderItemEntity> items) {
        this.items.clear();
        if (items != null) {
            items.forEach(this::addItem);
        }
    }

}