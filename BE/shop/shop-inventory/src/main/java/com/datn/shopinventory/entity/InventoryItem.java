package com.datn.shopinventory.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Entity
@Table(name = "inventory_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryItem {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false)
    private Long productId;


    @Column(nullable = false)
    private Integer stock;


    @Column(nullable = false)
    private Integer reservedQuantity = 0;


    @Column(nullable = false)
    private Integer availableQuantity;


    @Column(nullable = false)
    private BigDecimal importPrice;


    @Column(nullable = false)
    private BigDecimal sellingPrice;


    @PrePersist
    @PreUpdate
    private void calculateAvailable() {
        this.availableQuantity = this.stock - this.reservedQuantity;
    }

}