package com.datn.shopobject.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnItemResponse {

    private Long id;
    private Long productId;
    private String productName;

    private Integer quantity;
    private String reason;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private List<String> images = new ArrayList<>();
}
