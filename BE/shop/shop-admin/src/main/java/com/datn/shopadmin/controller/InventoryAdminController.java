package com.datn.shopadmin.controller;

import com.datn.shopclient.client.InventoryClient;
import com.datn.shopobject.dto.InventoryTransactionDTO;
import com.datn.shopobject.dto.request.*;
import com.datn.shopobject.dto.response.InventoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/inventory")
@RequiredArgsConstructor
public class InventoryAdminController {

    private final InventoryClient inventoryClient;

    // ============================
    // GET ALL (PAGE)
    // ============================
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<InventoryResponse> getAllInventory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return inventoryClient.getAllInventory(PageRequest.of(page, size));
    }

    // ============================
    // CREATE / UPDATE
    // ============================
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public InventoryResponse createOrUpdate(
            @RequestBody InventoryRequest req
    ) {
        return inventoryClient.createOrUpdate(req);
    }

    // ============================
    // GET BY VARIANT
    // ============================
    @GetMapping(value = "/{variantId}",produces = MediaType.APPLICATION_JSON_VALUE)
    public InventoryResponse getByVariant(
            @PathVariable Long variantId
    ) {
        return inventoryClient.getByVariantId(variantId);
    }

    // ============================
    // IMPORT
    // ============================
    @PostMapping(value = "/import",produces = MediaType.APPLICATION_JSON_VALUE)
    public InventoryResponse importStock(
            @RequestBody ImportRequest req
    ) {
        return inventoryClient.importStock(
                req.getVariantId(),
                req.getQuantity(),
                req.getImportPrice(),
                req.getNote()
        );
    }

    // ============================
    // EXPORT
    // ============================
    @PostMapping(value = "/export",produces = MediaType.APPLICATION_JSON_VALUE)
    public InventoryResponse exportStock(
            @RequestBody ExportRequest req
    ) {
        return inventoryClient.exportStock(
                req.getVariantId(),
                req.getQuantity(),
                req.getNote()
        );
    }

    // ============================
    // RESERVE
    // ============================
    @PostMapping(value = "/reserve",produces = MediaType.APPLICATION_JSON_VALUE)
    public void reserve(
            @RequestParam Long variantId,
            @RequestParam Integer quantity
    ) {
        inventoryClient.reserve(variantId, quantity);
    }

    // ============================
    // RELEASE
    // ============================
    @PostMapping(value = "/release",produces = MediaType.APPLICATION_JSON_VALUE)
    public void release(
            @RequestParam Long variantId,
            @RequestParam Integer quantity
    ) {
        inventoryClient.release(variantId, quantity);
    }

    // ============================
    // DEDUCT
    // ============================
    @PostMapping(value = "/deduct",produces = MediaType.APPLICATION_JSON_VALUE)
    public void deduct(
            @RequestParam Long variantId,
            @RequestParam Integer quantity
    ) {
        inventoryClient.deduct(variantId, quantity);
    }

    // ============================
    // ADJUST
    // ============================
    @PostMapping(value = "/adjust/{variantId}",produces = MediaType.APPLICATION_JSON_VALUE)
    public InventoryResponse adjust(
            @PathVariable Long variantId,
            @RequestBody AdjustRequest req
    ) {
        return inventoryClient.adjust(
                variantId,
                req.getNewStock(),
                req.getReason()
        );
    }

    // ============================
    // TRANSACTION LOGS
    // ============================
    @GetMapping("/transactions/{variantId}")
    public List<InventoryTransactionDTO> getTransactions(
            @PathVariable Long variantId
    ) {
        return inventoryClient.getTransactions(variantId);
    }
}
