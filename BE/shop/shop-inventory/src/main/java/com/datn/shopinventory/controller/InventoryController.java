package com.datn.shopinventory.controller;


import com.datn.shopinventory.dto.response.InventoryResponse;
import com.datn.shopinventory.dto.request.InventoryUpdateRequest;
import com.datn.shopinventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {


    private final InventoryService service;

    //Lấy tồn kho theo productId
    @GetMapping("/{productId}")
    public InventoryResponse get(@PathVariable("productId") Long productId) {
        return service.getByProductId(productId);
    }

   //update tồn kho
    @PutMapping
    public InventoryResponse update(@RequestBody InventoryUpdateRequest request) {
        return service.updateInventory(request);
    }

    //Đặt giữ tồn kho (reserve)
    @PostMapping("/reserve/{productId}/{qty}")
    public void reserve(@PathVariable("productId") Long productId, @PathVariable("qty") Integer qty) {
        service.reserveStock(productId, qty);
    }

    //Giải phóng tồn kho (release)
    @PostMapping("/release/{productId}/{qty}")
    public void release(@PathVariable("productId") Long productId, @PathVariable("qty") Integer qty) {
        service.releaseStock(productId, qty);
    }

    //Trừ tồn kho (deduct)
    @PostMapping("/deduct/{productId}/{qty}")
    public void deduct(@PathVariable("productId") Long productId, @PathVariable("qty") Integer qty) {
        service.deductStock(productId, qty);
    }
}
