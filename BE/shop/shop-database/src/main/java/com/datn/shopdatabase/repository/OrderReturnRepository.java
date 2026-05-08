package com.datn.shopdatabase.repository;

import com.datn.shopdatabase.entity.OrderReturnEntity;
import com.datn.shopdatabase.enums.ReturnStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderReturnRepository extends JpaRepository<OrderReturnEntity, Long> {

    Page<OrderReturnEntity> findByUserId(Long userId, Pageable pageable);

    List<OrderReturnEntity> findByOrderId(Long orderId);

    @Query("SELECT r FROM OrderReturnEntity r WHERE r.orderId = :orderId AND r.status != 'CANCELLED'")
    Optional<OrderReturnEntity> findActiveReturnByOrderId(@Param("orderId") Long orderId);

    Page<OrderReturnEntity> findByStatus(ReturnStatus status, Pageable pageable);

    @Query("SELECT r FROM OrderReturnEntity r WHERE r.status = 'PENDING' ORDER BY r.createdAt ASC")
    Page<OrderReturnEntity> findPendingReturns(Pageable pageable);
}