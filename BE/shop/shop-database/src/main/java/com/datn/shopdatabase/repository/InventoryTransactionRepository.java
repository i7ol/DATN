package com.datn.shopdatabase.repository;


import com.datn.shopdatabase.entity.InventoryTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InventoryTransactionRepository extends JpaRepository<InventoryTransactionEntity, Long> {
    List<InventoryTransactionEntity> findByVariantIdOrderByCreatedAtDesc(Long variantId);
}
