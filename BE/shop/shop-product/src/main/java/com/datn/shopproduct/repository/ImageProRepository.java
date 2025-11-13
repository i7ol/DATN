package com.datn.shopproduct.repository;

import com.datn.shopproduct.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;



public interface ImageProRepository extends JpaRepository<ProductImage, Long> {}
