package com.datn.shopobject.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {

    private Long id;

    /* ===== PRODUCT SNAPSHOT ===== */
    private Long productId;
    private String productName;

    /* ===== VARIANT SNAPSHOT ===== */
    private Long variantId;
    private String size;
    private String color;

    /* ===== PRICE SNAPSHOT ===== */
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal totalPrice;

    /* ===== AUDIT ===== */
    private Instant createdAt;
    private Instant updatedAt;
}
