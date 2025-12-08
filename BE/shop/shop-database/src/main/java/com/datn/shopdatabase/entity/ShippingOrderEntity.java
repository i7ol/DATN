package com.datn.shopdatabase.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingOrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId; // liên kết đến shop-order

    private String shippingCompany; // tên công ty vận chuyển
    private String shippingMethod;  // standard, express, ...

    private String trackingNumber;  // số vận đơn nếu có
    private Double shippingFee;

    private String recipientName;
    private String recipientPhone;
    private String recipientAddress;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(updatable = false)
    @org.hibernate.annotations.CreationTimestamp
    private Instant createdAt;

    @org.hibernate.annotations.UpdateTimestamp
    private Instant updatedAt;

    public enum Status {
        PENDING, SHIPPED, DELIVERED, CANCELLED
    }
}
