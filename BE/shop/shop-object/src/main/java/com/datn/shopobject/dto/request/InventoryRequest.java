package com.datn.shopobject.dto.request;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryRequest {
    private Long id;
    private Long variantId;
    private Integer stock;
    private BigDecimal importPrice;
    private BigDecimal sellingPrice;
}
