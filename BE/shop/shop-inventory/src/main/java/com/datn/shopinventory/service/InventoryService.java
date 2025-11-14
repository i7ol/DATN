package com.datn.shopinventory.service;


import com.datn.shopinventory.dto.response.InventoryResponse;
import com.datn.shopinventory.dto.request.InventoryUpdateRequest;


public interface InventoryService {
    InventoryResponse getByProductId(Long productId);
    InventoryResponse updateInventory(InventoryUpdateRequest request);
    void reserveStock(Long productId, Integer qty);
    void releaseStock(Long productId, Integer qty);
    void deductStock(Long productId, Integer qty);
}
