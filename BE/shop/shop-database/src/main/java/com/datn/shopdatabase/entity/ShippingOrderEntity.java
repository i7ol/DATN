package com.datn.shopdatabase.entity;

import com.datn.shopdatabase.enums.StatusEnum;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "shipping_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingOrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;
    private Long userId;

    private String shippingCompany;
    private String shippingMethod;
    private String trackingNumber;
    private Double shippingFee;
    private Integer estimatedDeliveryDays;
    private LocalDate estimatedDeliveryDate;

    // Thông tin người nhận
    private String recipientName;
    private String recipientPhone;
    private String recipientEmail;
    private String recipientAddress;
    private String recipientProvince;
    private String recipientDistrict;
    private String recipientWard;

    // Trạng thái
    @Enumerated(EnumType.STRING)
    private StatusEnum status;

    // Thời gian các sự kiện
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime lastSyncAt;

    // Ghi chú
    private String notes;

    // Vị trí hiện tại (từ tracking)
    private String currentLocation;

    @Column(updatable = false)
    @org.hibernate.annotations.CreationTimestamp
    private Instant createdAt;

    @org.hibernate.annotations.UpdateTimestamp
    private Instant updatedAt;

}