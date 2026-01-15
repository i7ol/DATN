package com.datn.shopclient.client;

import com.datn.shopobject.dto.InventoryTransactionDTO;
import com.datn.shopobject.dto.request.InventoryRequest;
import com.datn.shopobject.dto.response.InventoryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@FeignClient(
        name = "inventory-service",
        url = "${inventory.service.url}"
)
public interface InventoryClient {

    // =============================
    // CREATE / UPDATE
    // =============================
    @PostMapping("/internal/inventory")
    InventoryResponse createOrUpdate(
            @RequestBody InventoryRequest request
    );

    // =============================
    // GET BY VARIANT
    // =============================
    @GetMapping("/internal/inventory/{variantId}")
    InventoryResponse getByVariantId(
            @PathVariable Long variantId
    );

    // =============================
    // LIST (PAGE)
    // =============================
    @GetMapping("/internal/inventory")
    Page<InventoryResponse> getAll(
            Pageable pageable
    );

    // =============================
    // RESERVE
    // =============================
    @PostMapping("/internal/inventory/reserve")
    void reserve(
            @RequestParam Long variantId,
            @RequestParam Integer qty
    );

    // =============================
    // RELEASE
    // =============================
    @PostMapping("/internal/inventory/release")
    void release(
            @RequestParam Long variantId,
            @RequestParam Integer qty
    );

    // =============================
    // DEDUCT
    // =============================
    @PostMapping("/internal/inventory/deduct")
    void deduct(
            @RequestParam Long variantId,
            @RequestParam Integer qty
    );

    // =============================
    // IMPORT
    // =============================
    @PostMapping("/internal/inventory/import")
    InventoryResponse importStock(
            @RequestParam Long variantId,
            @RequestParam Integer qty,
            @RequestParam(required = false) BigDecimal importPrice,
            @RequestParam(required = false) String note
    );

    // =============================
    // EXPORT
    // =============================
    @PostMapping("/internal/inventory/export")
    InventoryResponse exportStock(
            @RequestParam Long variantId,
            @RequestParam Integer qty,
            @RequestParam(required = false) String note
    );

    // =============================
    // ADJUST
    // =============================
    @PostMapping("/internal/inventory/adjust")
    InventoryResponse adjust(
            @RequestParam Long variantId,
            @RequestParam Integer newStock,
            @RequestParam(required = false) String reason
    );

    // =============================
    // TRANSACTION LOGS
    // =============================
    @GetMapping("/internal/inventory/{variantId}/transactions")
    List<InventoryTransactionDTO> getTransactions(
            @PathVariable Long variantId
    );
}
