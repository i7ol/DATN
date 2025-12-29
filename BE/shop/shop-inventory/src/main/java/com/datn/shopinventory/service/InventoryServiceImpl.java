package com.datn.shopinventory.service;

import com.datn.shopdatabase.entity.*;
import com.datn.shopdatabase.enums.TransactionType;
import com.datn.shopdatabase.repository.InventoryRepository;
import com.datn.shopdatabase.repository.InventoryTransactionRepository;
import com.datn.shopdatabase.repository.ProductVariantRepository;
import com.datn.shopobject.dto.InventoryTransactionDTO;
import com.datn.shopobject.dto.request.InventoryRequest;
import com.datn.shopobject.dto.response.InventoryResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository repository;
    private final InventoryTransactionRepository txRepository;
    private final ProductVariantRepository variantRepository;

    // =============================
    // CREATE / UPDATE
    // =============================
    @Override
    @Transactional
    public InventoryResponse createOrUpdate(InventoryRequest req) {
        if (req.getVariantId() == null) throw new IllegalArgumentException("variantId required");

        InventoryItemEntity item = repository.findByVariantId(req.getVariantId())
                .orElseGet(() -> InventoryItemEntity.builder()
                        .variantId(req.getVariantId())
                        .stock(0)
                        .reservedQuantity(0)
                        .build()
                );

        if (req.getStock() != null) item.setStock(req.getStock());
        if (req.getImportPrice() != null) item.setImportPrice(req.getImportPrice());
        if (req.getSellingPrice() != null) item.setSellingPrice(req.getSellingPrice());

        repository.save(item);
        return toResponse(item);
    }

    // =============================
    // GET BY VARIANT
    // =============================
    @Override
    public InventoryResponse getByVariantId(Long variantId) {
        InventoryItemEntity item = repository.findByVariantId(variantId)
                .orElseThrow(() -> new RuntimeException("Inventory not found"));

        return toResponse(item);
    }

    // =============================
    // LIST
    // =============================
    @Override
    public Page<InventoryResponse> getAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toResponse);
    }

    // =============================
    // RESERVE
    // =============================
    @Override
    @Transactional
    public void reserve(Long variantId, Integer qty) {
        InventoryItemEntity item = repository.findByVariantId(variantId)
                .orElseThrow(() -> new RuntimeException("Not found"));

        if (item.getAvailableQuantity() < qty)
            throw new RuntimeException("Not enough available");

        item.setReservedQuantity(item.getReservedQuantity() + qty);
        repository.save(item);

        logTx(item, variantId, TransactionType.RESERVE, qty, "reserve");
    }

    // =============================
    // RELEASE
    // =============================
    @Override
    @Transactional
    public void release(Long variantId, Integer qty) {
        InventoryItemEntity item = repository.findByVariantId(variantId)
                .orElseThrow(() -> new RuntimeException("Not found"));

        item.setReservedQuantity(Math.max(0, item.getReservedQuantity() - qty));
        repository.save(item);

        logTx(item, variantId, TransactionType.RELEASE, -qty, "release");
    }

    // =============================
    // DEDUCT (bán hàng)
    // =============================
    @Override
    @Transactional
    public void deduct(Long variantId, Integer qty) {
        InventoryItemEntity item = repository.findByVariantId(variantId)
                .orElseThrow(() -> new RuntimeException("Not found"));

        if (item.getStock() < qty) throw new RuntimeException("Not enough stock");

        item.setStock(item.getStock() - qty);
        item.setReservedQuantity(Math.max(0, item.getReservedQuantity() - qty));
        repository.save(item);

        logTx(item, variantId, TransactionType.DEDUCT, -qty, "deduct sale");
    }

    // =============================
    // IMPORT
    // =============================
    @Override
    @Transactional
    public InventoryResponse importStock(Long variantId, Integer qty, BigDecimal importPrice, String note) {
        InventoryItemEntity item = repository.findByVariantId(variantId)
                .orElseGet(() -> InventoryItemEntity.builder()
                        .variantId(variantId)
                        .stock(0)
                        .reservedQuantity(0)
                        .build()
                );

        item.setStock(item.getStock() + qty);

        if (importPrice != null)
            item.setImportPrice(importPrice);

        repository.save(item);

        logTx(item, variantId, TransactionType.IMPORT, qty, note);

        return toResponse(item);
    }

    // =============================
    // EXPORT
    // =============================
    @Override
    @Transactional
    public InventoryResponse exportStock(Long variantId, Integer qty, String note) {
        InventoryItemEntity item = repository.findByVariantId(variantId)
                .orElseThrow(() -> new RuntimeException("Not found"));

        if (item.getStock() < qty) throw new RuntimeException("Not enough stock");

        item.setStock(item.getStock() - qty);
        repository.save(item);

        logTx(item, variantId, TransactionType.EXPORT, -qty, note);

        return toResponse(item);
    }

    // =============================
    // ADJUST
    // =============================
    @Override
    @Transactional
    public InventoryResponse adjust(Long variantId, Integer newStock, String reason) {
        InventoryItemEntity item = repository.findByVariantId(variantId)
                .orElseThrow(() -> new RuntimeException("Not found"));

        int delta = newStock - item.getStock();
        item.setStock(newStock);
        repository.save(item);

        logTx(item, variantId, TransactionType.ADJUST, delta, reason);

        return toResponse(item);
    }

    // =============================
    // GET TRANSACTION LOGS
    // =============================
    @Override
    public List<InventoryTransactionDTO> getTransactions(Long variantId) {
        return txRepository.findByVariantIdOrderByCreatedAtDesc(variantId)
                .stream()
                .map(tx -> new InventoryTransactionDTO(
                        tx.getId(),
                        tx.getVariantId(),
                        // CHỈNH: convert enum -> String
                        tx.getType() != null ? tx.getType().name() : null,
                        tx.getQuantity(),
                        tx.getNote()
                ))
                .collect(Collectors.toList());
    }

    // =============================
    // LOG TRANSACTION
    // =============================
    private void logTx(InventoryItemEntity item, Long variantId,
                       TransactionType type, Integer qty, String note) {

        InventoryTransactionEntity tx = InventoryTransactionEntity.builder()
                .inventoryItemId(item.getId())
                .variantId(variantId)
                .type(type)
                .quantity(qty)
                .note(note)
                .build();

        txRepository.save(tx);
    }

    // =============================
    // MAP TO RESPONSE (không dùng mapper)
    // =============================
    private InventoryResponse toResponse(InventoryItemEntity item) {

        ProductVariantEntity variant = variantRepository.findById(item.getVariantId())
                .orElse(null);

        String color = null, size = null, productName = null, thumbnail = null;

        if (variant != null) {
            color = variant.getColor();
            size = variant.getSizeName();

            ProductEntity product = variant.getProduct();
            if (product != null) {
                productName = product.getName();
                if (product.getImages() != null && !product.getImages().isEmpty()) {
                    thumbnail = product.getImages().stream()
                            .findFirst()  // Lấy phần tử đầu tiên trong Set
                            .map(ProductImageEntity::getUrl)
                            .orElse(null);
                }
            }
        }

        return new InventoryResponse(
                item.getId(),
                item.getVariantId(),
                item.getStock(),
                item.getReservedQuantity(),
                item.getAvailableQuantity(),
                item.getImportPrice(),
                item.getSellingPrice(),
                color,
                size,
                productName,
                thumbnail
        );
    }
}