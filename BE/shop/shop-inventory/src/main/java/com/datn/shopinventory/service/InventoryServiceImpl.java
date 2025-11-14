package com.datn.shopinventory.service;

import com.datn.shopinventory.dto.response.InventoryResponse;
import com.datn.shopinventory.dto.request.InventoryUpdateRequest;
import com.datn.shopinventory.entity.InventoryItem;
import com.datn.shopinventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository repository;

    @Override
    public InventoryResponse getByProductId(Long productId) {
        InventoryItem item = repository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found"));
        return toResponse(item);
    }

    @Override
    public InventoryResponse updateInventory(InventoryUpdateRequest request) {
        InventoryItem item = repository.findByProductId(request.productId())
                .orElseGet(() -> {
                    InventoryItem i = new InventoryItem();
                    i.setProductId(request.productId());
                    return i;
                });

        item.setStock(request.stock());
        if (request.importPrice() != null) item.setImportPrice(request.importPrice());
        if (request.sellingPrice() != null) item.setSellingPrice(request.sellingPrice());

        repository.save(item);
        return toResponse(item);
    }

    @Override
    public void reserveStock(Long productId, Integer qty) {
        InventoryItem item = repository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found"));

        if (item.getAvailableQuantity() < qty)
            throw new RuntimeException("Not enough stock");

        item.setReservedQuantity(item.getReservedQuantity() + qty);
        repository.save(item);
    }

    @Override
    public void releaseStock(Long productId, Integer qty) {
        InventoryItem item = repository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found"));

        item.setReservedQuantity(Math.max(0, item.getReservedQuantity() - qty));
        repository.save(item);
    }

    @Override
    public void deductStock(Long productId, Integer qty) {
        InventoryItem item = repository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found"));

        if (item.getStock() < qty)
            throw new RuntimeException("Not enough stock");

        item.setStock(item.getStock() - qty);
        item.setReservedQuantity(Math.max(0, item.getReservedQuantity() - qty));
        repository.save(item);
    }

    private InventoryResponse toResponse(InventoryItem item) {
        return new InventoryResponse(
                item.getId(),
                item.getProductId(),
                item.getStock(),
                item.getReservedQuantity(),
                item.getAvailableQuantity(),
                item.getImportPrice(),
                item.getSellingPrice()
        );
    }
}
