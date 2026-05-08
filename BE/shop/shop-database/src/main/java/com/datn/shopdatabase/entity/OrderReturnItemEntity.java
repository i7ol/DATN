package com.datn.shopdatabase.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_return_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderReturnItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_id", nullable = false)
    private OrderReturnEntity orderReturn;

    private Long orderItemId;      // liên kết với OrderItemEntity
    private Long productId;

    @Column(nullable = false)
    private Integer quantity;

    private String reason;
}