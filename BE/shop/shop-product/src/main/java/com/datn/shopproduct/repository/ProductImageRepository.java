package com.datn.shopproduct.repository;

import com.datn.shopproduct.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ProductImageRepository  extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProductId(Long productId);

    ProductImage findFirstByProductId(Long productId);}
