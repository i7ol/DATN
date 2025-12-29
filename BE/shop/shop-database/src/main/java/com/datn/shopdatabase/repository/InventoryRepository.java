package com.datn.shopdatabase.repository;


import com.datn.shopdatabase.entity.InventoryItemEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryItemEntity, Long> {
    Optional<InventoryItemEntity> findByVariantId(Long variantId);
    List<InventoryItemEntity> findByVariantIdIn(List<Long> variantIds);
    Page<InventoryItemEntity> findAll(Pageable pageable);
}