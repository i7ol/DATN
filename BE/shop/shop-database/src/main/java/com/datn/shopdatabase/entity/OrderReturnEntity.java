package com.datn.shopdatabase.entity;

import com.datn.shopdatabase.enums.ReturnStatus;
import com.datn.shopdatabase.enums.ReturnType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "order_returns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderReturnEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    private Long userId;
    private String guestId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReturnType returnType;

    @Column(nullable = false, length = 500)
    private String reason;

    @Column(columnDefinition = "CLOB")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReturnStatus status = ReturnStatus.PENDING;

    private BigDecimal refundAmount;
    private String refundTransactionId;

    @Column(columnDefinition = "CLOB")
    private String adminNote;
    private BigDecimal totalReturnValue;
    private String returnTrackingCode;   // Mã vận chuyển khi khách trả hàng

    private Instant processedDate;
    private Instant completedDate;

    // ==================== RELATIONSHIPS ====================

    @OneToMany(mappedBy = "orderReturn", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderReturnItemEntity> items = new ArrayList<>();

    @OneToMany(mappedBy = "orderReturn", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReturnImageEntity> images = new ArrayList<>();


    // Helper method
    public void addItem(OrderReturnItemEntity item) {
        item.setOrderReturn(this);
        this.items.add(item);
    }

    public void addImage(ReturnImageEntity image) {
        image.setOrderReturn(this);
        this.images.add(image);
    }
}