package com.datn.shopinventory.dto.response;

import java.math.BigDecimal;

public record InventoryResponse(
        Long id,
        Long productId,
        Integer stock,
        Integer reservedQuantity,
        Integer availableQuantity,
        BigDecimal importPrice,
        BigDecimal sellingPrice
) {}
