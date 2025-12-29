package com.datn.shopdatabase.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* ===== SNAPSHOT PRODUCT / VARIANT ===== */

    // Variant
    @Column(name = "variant_id")
    private Long variantId;

    // Product
    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    // Variant attributes
    @Column(name = "size")
    private String size;

    @Column(name = "color")
    private String color;

    /* ===== PRICE SNAPSHOT ===== */

    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice;

    /* ===== RELATION ===== */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private OrderEntity order;
}
