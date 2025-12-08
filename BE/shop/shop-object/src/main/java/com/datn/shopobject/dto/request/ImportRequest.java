package com.datn.shopobject.dto.request;

import lombok.*;
import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor
public class ImportRequest {
    private Long variantId;
    private Integer quantity;
    private BigDecimal importPrice;
    private String note;
}
