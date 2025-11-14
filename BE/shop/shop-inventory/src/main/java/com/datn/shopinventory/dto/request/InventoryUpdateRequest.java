package com.datn.shopinventory.dto.request;

import java.math.BigDecimal;


public record InventoryUpdateRequest(
        Long productId,
        Integer stock,
        BigDecimal importPrice,
        BigDecimal sellingPrice
) {}
