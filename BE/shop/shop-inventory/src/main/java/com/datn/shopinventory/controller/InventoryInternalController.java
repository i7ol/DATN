package com.datn.shopinventory.controller;

import com.datn.shopinventory.service.InventoryService;
import com.datn.shopobject.dto.InventoryTransactionDTO;
import com.datn.shopobject.dto.request.InventoryRequest;
import com.datn.shopobject.dto.response.InventoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
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
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public InventoryResponse createOrUpdate(
            @RequestBody InventoryRequest request
    ) {
        return inventoryService.createOrUpdate(request);
    }

    // =============================
    // GET BY VARIANT
    // =============================
    @GetMapping(value = "/{variantId}",produces = MediaType.APPLICATION_JSON_VALUE)
    public InventoryResponse getByVariantId(
            @PathVariable Long variantId
    ) {
        return inventoryService.getByVariantId(variantId);
    }

    // =============================
    // RESERVE
    // =============================
    @PostMapping(value = "/reserve",produces = MediaType.APPLICATION_JSON_VALUE)
    public void reserve(
            @RequestParam Long variantId,
            @RequestParam Integer qty
    ) {
        inventoryService.reserve(variantId, qty);
    }

    // =============================
    // RELEASE
    // =============================
    @PostMapping(value = "/release",produces = MediaType.APPLICATION_JSON_VALUE)
    public void release(
            @RequestParam Long variantId,
            @RequestParam Integer qty
    ) {
        inventoryService.release(variantId, qty);
    }

    // =============================
    // DEDUCT
    // =============================
    @PostMapping(value = "/deduct",produces = MediaType.APPLICATION_JSON_VALUE)
    public void deduct(
            @RequestParam Long variantId,
            @RequestParam Integer qty
    ) {
        inventoryService.deduct(variantId, qty);
    }

    // =============================
    // IMPORT
    // =============================
    @PostMapping(value = "/import",produces = MediaType.APPLICATION_JSON_VALUE)
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
    @PostMapping(value = "/export",produces = MediaType.APPLICATION_JSON_VALUE)
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
    @PostMapping(value = "/adjust",produces = MediaType.APPLICATION_JSON_VALUE)
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
    @GetMapping(value = "/{variantId}/transactions",produces = MediaType.APPLICATION_JSON_VALUE)
    public List<InventoryTransactionDTO> getTransactions(
            @PathVariable Long variantId
    ) {
        return inventoryService.getTransactions(variantId);
    }

    // =============================
    // LIST (PAGE)
    // =============================
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<InventoryResponse> getAllInventory(Pageable pageable) {
        return inventoryService.getAllInventory(pageable);
    }
}
