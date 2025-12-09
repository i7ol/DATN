package com.datn.shopobject.dto.response;

import com.datn.shopobject.dto.ImageDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemResponse {
    private Long productId;
    private String productName;
    private int quantity;
    private BigDecimal price;
    private List<ImageDTO> images;
}

