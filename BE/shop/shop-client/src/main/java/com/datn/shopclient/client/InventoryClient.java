package com.datn.shopclient.client;

import com.datn.shopobject.dto.InventoryTransactionDTO;
import com.datn.shopobject.dto.request.InventoryRequest;
import com.datn.shopobject.dto.response.InventoryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
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
    @PostMapping(value = "/internal/inventory",produces = MediaType.APPLICATION_JSON_VALUE)
    InventoryResponse createOrUpdate(
            @RequestBody InventoryRequest request
    );

    // =============================
    // GET BY VARIANT
    // =============================
    @GetMapping(value = "/internal/inventory/{variantId}",produces = MediaType.APPLICATION_JSON_VALUE)
    InventoryResponse getByVariantId(
            @PathVariable Long variantId
    );

    // =============================
    // LIST (PAGE)
    // =============================
    @GetMapping(value = "/internal/inventory",produces = MediaType.APPLICATION_JSON_VALUE)
    Page<InventoryResponse> getAllInventory(
            Pageable pageable
    );

    // =============================
    // RESERVE
    // =============================
    @PostMapping(value = "/internal/inventory/reserve",produces = MediaType.APPLICATION_JSON_VALUE)
    void reserve(
            @RequestParam Long variantId,
            @RequestParam Integer qty
    );

    // =============================
    // RELEASE
    // =============================
    @PostMapping(value = "/internal/inventory/release",produces = MediaType.APPLICATION_JSON_VALUE)
    void release(
            @RequestParam Long variantId,
            @RequestParam Integer qty
    );

    // =============================
    // DEDUCT
    // =============================
    @PostMapping(value = "/internal/inventory/deduct",produces = MediaType.APPLICATION_JSON_VALUE)
    void deduct(
            @RequestParam Long variantId,
            @RequestParam Integer qty
    );

    // =============================
    // IMPORT
    // =============================
    @PostMapping(value = "/internal/inventory/import",produces = MediaType.APPLICATION_JSON_VALUE)
    InventoryResponse importStock(
            @RequestParam Long variantId,
            @RequestParam Integer qty,
            @RequestParam(required = false) BigDecimal importPrice,
            @RequestParam(required = false) String note
    );

    // =============================
    // EXPORT
    // =============================
    @PostMapping(value = "/internal/inventory/export",produces = MediaType.APPLICATION_JSON_VALUE)
    InventoryResponse exportStock(
            @RequestParam Long variantId,
            @RequestParam Integer qty,
            @RequestParam(required = false) String note
    );

    // =============================
    // ADJUST
    // =============================
    @PostMapping(value = "/internal/inventory/adjust",produces = MediaType.APPLICATION_JSON_VALUE)
    InventoryResponse adjust(
            @RequestParam Long variantId,
            @RequestParam Integer newStock,
            @RequestParam(required = false) String reason
    );

    // =============================
    // TRANSACTION LOGS
    // =============================
    @GetMapping(value = "/internal/inventory/{variantId}/transactions",produces = MediaType.APPLICATION_JSON_VALUE)
    List<InventoryTransactionDTO> getTransactions(
            @PathVariable Long variantId
    );
}
