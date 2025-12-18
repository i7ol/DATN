package com.datn.shopdatabase.repository;

import com.datn.shopdatabase.entity.ShippingOrderEntity;
import com.datn.shopdatabase.enums.StatusEnum; // THÊM IMPORT
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShippingRepository extends JpaRepository<ShippingOrderEntity, Long> {

    List<ShippingOrderEntity> findByOrderId(Long orderId);

    List<ShippingOrderEntity> findByUserId(Long userId);

    Optional<ShippingOrderEntity> findByIdAndUserId(Long id, Long userId);

    List<ShippingOrderEntity> findByStatus(StatusEnum status); // SỬA: StatusEnum

    long countByStatus(StatusEnum status); // SỬA: StatusEnum

    @Query("SELECT s FROM ShippingOrderEntity s WHERE " +
            "(:status IS NULL OR s.status = :status) AND " +
            "(:company IS NULL OR s.shippingCompany LIKE %:company%) AND " +
            "(:fromDate IS NULL OR s.createdAt >= :fromDate) AND " +
            "(:toDate IS NULL OR s.createdAt <= :toDate)")
    Page<ShippingOrderEntity> findAllWithFilters(
            @Param("status") StatusEnum status, // SỬA: StatusEnum
            @Param("company") String company,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable);

    @Query("SELECT s FROM ShippingOrderEntity s WHERE s.lastSyncAt IS NULL OR s.lastSyncAt < :syncBefore")
    List<ShippingOrderEntity> findShippingsNeedSync(@Param("syncBefore") LocalDateTime syncBefore);
}