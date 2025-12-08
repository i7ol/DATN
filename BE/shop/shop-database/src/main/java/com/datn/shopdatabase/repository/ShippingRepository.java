package com.datn.shopdatabase.repository;

import com.datn.shopdatabase.entity.ShippingOrderEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ShippingRepository extends JpaRepository<ShippingOrderEntity, Long> {
    List<ShippingOrderEntity> findByOrderId(Long orderId);
}
