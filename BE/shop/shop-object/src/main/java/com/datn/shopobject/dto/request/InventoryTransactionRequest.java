package com.datn.shopobject.dto.request;

import lombok.Data;

@Data
public class InventoryTransactionRequest {
    private Long variantId;
    private String type; // IMPORT, EXPORT, RESERVE, RELEASE, DEDUCT, ADJUST
    private Integer quantity;
    private String note;
}

