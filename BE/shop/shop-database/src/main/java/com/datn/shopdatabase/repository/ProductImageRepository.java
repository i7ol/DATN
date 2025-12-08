package com.datn.shopdatabase.repository;


import com.datn.shopdatabase.entity.ProductImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ProductImageRepository  extends JpaRepository<ProductImageEntity, Long> {
    List<ProductImageEntity> findByProductId(Long productId);

    ProductImageEntity findFirstByProductId(Long productId);

    void deleteAllByProductId(Long id);
}
