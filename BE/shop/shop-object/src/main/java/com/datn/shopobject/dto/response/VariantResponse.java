package com.datn.shopobject.dto.response;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VariantResponse {
    private Long id;
    private String sizeName;
    private String color;

    private Integer stock;
    private Integer reservedQuantity;
    private Integer availableQuantity;

    private BigDecimal importPrice;
    private BigDecimal sellingPrice;

    private String thumbnail;
}
