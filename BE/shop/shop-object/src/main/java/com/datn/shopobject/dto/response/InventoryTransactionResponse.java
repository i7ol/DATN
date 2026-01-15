package com.datn.shopobject.dto.response;

import lombok.Data;

@Data
public class InventoryTransactionResponse {
    private Long id;
    private Long variantId;
    private String type;
    private Integer quantity;
    private String note;
}
