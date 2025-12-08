package com.datn.shopobject.dto.response;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {
    private Long id;
    private Long variantId;

    private Integer stock;
    private Integer reservedQuantity;
    private Integer availableQuantity;

    private BigDecimal importPrice;
    private BigDecimal sellingPrice;

    private String color;
    private String size;
    private String productName;
    private String thumbnail;
}
