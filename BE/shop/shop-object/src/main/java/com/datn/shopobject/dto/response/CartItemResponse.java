package com.datn.shopobject.dto.response;

import com.datn.shopobject.dto.ImageDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemResponse {
    private Long productId;
    private String productName;
    private Long variantId;
    private String size;
    private String color;

    @Builder.Default
    private int quantity = 0;

    @Builder.Default
    private BigDecimal price = BigDecimal.ZERO;

    @Builder.Default
    private List<ImageDTO> images = new ArrayList<>();
}