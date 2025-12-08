package com.datn.shopdatabase.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "inventory_items", uniqueConstraints = @UniqueConstraint(columnNames = {"variant_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryItemEntity extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long variantId;

    @Column(nullable = false)
    private Integer stock = 0;

    @Column(nullable = false)
    private Integer reservedQuantity = 0;

    @Column(nullable = false)
    private Integer availableQuantity = 0;

    @Column(precision = 19, scale = 2)
    private BigDecimal importPrice;

    @Column(precision = 19, scale = 2)
    private BigDecimal sellingPrice;

    @PrePersist @PreUpdate
    public void calcAvailable() {
        if (stock == null) stock = 0;
        if (reservedQuantity == null) reservedQuantity = 0;
        this.availableQuantity = Math.max(0, stock - reservedQuantity);
    }
}