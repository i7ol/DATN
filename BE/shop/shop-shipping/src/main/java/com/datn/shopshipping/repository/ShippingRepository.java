package com.datn.shopshipping.repository;

import com.datn.shopshipping.entity.ShippingOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ShippingRepository extends JpaRepository<ShippingOrder, Long> {
    List<ShippingOrder> findByOrderId(Long orderId);
}
