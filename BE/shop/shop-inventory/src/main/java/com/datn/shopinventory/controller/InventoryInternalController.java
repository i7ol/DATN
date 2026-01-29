package com.datn.shopinventory.controller;

import com.datn.shopinventory.service.InventoryService;
import com.datn.shopobject.dto.InventoryTransactionDTO;
import com.datn.shopobject.dto.request.InventoryRequest;
import com.datn.shopobject.dto.response.InventoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/internal/inventory")
@RequiredArgsConstructor
public class InventoryInternalController {

    private final InventoryService inventoryService;

    // =============================
    // CREATE / UPDATE
    // =============================
    @PostMapping
    public InventoryResponse createOrUpdate(
            @RequestBody InventoryRequest request
    ) {
        return inventoryService.createOrUpdate(request);
    }

    // =============================
    // GET BY VARIANT
    // =============================
    @GetMapping("/{variantId}")
    public InventoryResponse getByVariantId(
            @PathVariable Long variantId
    ) {
        return inventoryService.getByVariantId(variantId);
    }

    // =============================
    // RESERVE
    // =============================
    @PostMapping("/reserve")
    public void reserve(
            @RequestParam Long variantId,
            @RequestParam Integer qty
    ) {
        inventoryService.reserve(variantId, qty);
    }

    // =============================
    // RELEASE
    // =============================
    @PostMapping("/release")
    public void release(
            @RequestParam Long variantId,
            @RequestParam Integer qty
    ) {
        inventoryService.release(variantId, qty);
    }

    // =============================
    // DEDUCT
    // =============================
    @PostMapping("/deduct")
    public void deduct(
            @RequestParam Long variantId,
            @RequestParam Integer qty
    ) {
        inventoryService.deduct(variantId, qty);
    }

    // =============================
    // IMPORT
    // =============================
    @PostMapping("/import")
    public InventoryResponse importStock(
            @RequestParam Long variantId,
            @RequestParam Integer qty,
            @RequestParam(required = false) BigDecimal importPrice,
            @RequestParam(required = false) String note
    ) {
        return inventoryService.importStock(variantId, qty, importPrice, note);
    }

    // =============================
    // EXPORT
    // =============================
    @PostMapping("/export")
    public InventoryResponse exportStock(
            @RequestParam Long variantId,
            @RequestParam Integer qty,
            @RequestParam(required = false) String note
    ) {
        return inventoryService.exportStock(variantId, qty, note);
    }

    // =============================
    // ADJUST
    // =============================
    @PostMapping("/adjust")
    public InventoryResponse adjust(
            @RequestParam Long variantId,
            @RequestParam Integer newStock,
            @RequestParam(required = false) String reason
    ) {
        return inventoryService.adjust(variantId, newStock, reason);
    }

    // =============================
    // TRANSACTION LOGS
    // =============================
    @GetMapping("/{variantId}/transactions")
    public List<InventoryTransactionDTO> getTransactions(
            @PathVariable Long variantId
    ) {
        return inventoryService.getTransactions(variantId);
    }

    // =============================
    // LIST (PAGE)
    // =============================
    @GetMapping
    public Page<InventoryResponse> getAllInventory(Pageable pageable) {
        return inventoryService.getAllInventory(pageable);
    }
}
