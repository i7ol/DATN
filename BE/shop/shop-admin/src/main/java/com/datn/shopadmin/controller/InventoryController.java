package com.datn.shopadmin.controller;

import com.datn.shopobject.dto.request.*;
import com.datn.shopobject.dto.response.InventoryResponse;
import com.datn.shopobject.dto.InventoryTransactionDTO;
import com.datn.shopinventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/admin/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService service;

    // ============================
    // GET ALL (PAGE)
    // ============================
    @GetMapping
    public Page<InventoryResponse> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return service.getAll(PageRequest.of(page, size));
    }

    // ============================
    // CREATE / UPDATE
    // ============================
    @PostMapping
    public InventoryResponse createOrUpdate(@RequestBody InventoryRequest req) {
        return service.createOrUpdate(req);
    }

    // ============================
    // GET BY VARIANT
    // ============================
    @GetMapping("/{variantId}")
    public InventoryResponse getByVariant(@PathVariable("variantId") Long variantId) {
        return service.getByVariantId(variantId);
    }

    // ============================
    // IMPORT
    // ============================
    @PostMapping("/import")
    public InventoryResponse importStock(@RequestBody ImportRequest req) {
        return service.importStock(
                req.getVariantId(),
                req.getQuantity(),
                req.getImportPrice(),
                req.getNote()
        );
    }

    // ============================
    // EXPORT
    // ============================
    @PostMapping("/export")
    public InventoryResponse exportStock(@RequestBody ExportRequest req) {
        return service.exportStock(
                req.getVariantId(),
                req.getQuantity(),
                req.getNote()
        );
    }

    // ============================
    // RESERVE
    // ============================
    @PostMapping("/reserve")
    public void reserve(@RequestParam("variantId") Long variantId, @RequestParam("quantity") Integer quantity) {
        service.reserve(variantId, quantity);
    }

    // ============================
    // RELEASE
    // ============================
    @PostMapping("/release")
    public void release(@RequestParam("variantId") Long variantId, @RequestParam("quantity") Integer quantity) {
        service.release(variantId, quantity);
    }

    // ============================
    // DEDUCT (SALE)
    // ============================
    @PostMapping("/deduct")
    public void deduct(@RequestParam("variantId") Long variantId, @RequestParam("quantity") Integer quantity) {
        service.deduct(variantId, quantity);
    }

    // ============================
    // ADJUST
    // ============================
    @PostMapping("/adjust/{variantId}")
    public InventoryResponse adjust(@PathVariable("variantId") Long variantId, @RequestBody AdjustRequest req) {
        return service.adjust(variantId, req.getNewStock(), req.getReason());
    }

    // ============================
    // TRANSACTION LOGS
    // ============================
    @GetMapping("/transactions/{variantId}")
    public List<InventoryTransactionDTO> getTransactions(@PathVariable("variantId") Long variantId) {
        return service.getTransactions(variantId);
    }
}
