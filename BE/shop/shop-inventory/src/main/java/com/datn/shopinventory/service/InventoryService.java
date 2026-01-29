package com.datn.shopinventory.service;

import com.datn.shopobject.dto.InventoryTransactionDTO;
import com.datn.shopobject.dto.request.InventoryRequest;
import com.datn.shopobject.dto.response.InventoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface InventoryService {
    InventoryResponse createOrUpdate(InventoryRequest req);
    InventoryResponse getByVariantId(Long variantId);
    void reserve(Long variantId, Integer qty);
    void release(Long variantId, Integer qty);
    void deduct(Long variantId, Integer qty);
    Page<InventoryResponse> getAllInventory(Pageable pageable);
    InventoryResponse importStock(Long variantId, Integer qty, BigDecimal importPrice, String note);
    InventoryResponse exportStock(Long variantId, Integer qty, String note);
    InventoryResponse adjust(Long variantId, Integer newStock, String reason);
    List<InventoryTransactionDTO> getTransactions(Long variantId);
}

