package com.datn.shopdatabase.repository;

import com.datn.shopdatabase.entity.PaymentEntity;
import com.datn.shopdatabase.enums.PaymentMethod;
import com.datn.shopdatabase.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    Optional<PaymentEntity> findByOrderId(Long orderId);

    List<PaymentEntity> findByUserId(Long userId);

    List<PaymentEntity> findByStatus(PaymentStatus status);

    List<PaymentEntity> findByMethod(PaymentMethod method);

    @Query("SELECT p FROM PaymentEntity p WHERE " +
            "(:status IS NULL OR p.status = :status) AND " +
            "(:method IS NULL OR p.method = :method) AND " +
            "(:fromDate IS NULL OR p.createdAt >= :fromDate) AND " +
            "(:toDate IS NULL OR p.createdAt <= :toDate) AND " +
            "(:minAmount IS NULL OR p.amount >= :minAmount) AND " +
            "(:maxAmount IS NULL OR p.amount <= :maxAmount)")
    Page<PaymentEntity> findAllWithFilters(
            @Param("status") PaymentStatus status,
            @Param("method") PaymentMethod method,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            Pageable pageable);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PaymentEntity p WHERE " +
            "p.status = :status AND " +
            "(:fromDate IS NULL OR DATE(p.createdAt) >= :fromDate) AND " +
            "(:toDate IS NULL OR DATE(p.createdAt) <= :toDate)")
    BigDecimal getTotalAmountByPeriodAndStatus(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("status") PaymentStatus status);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    long countByStatusAndCreatedAtBetween(PaymentStatus status, LocalDateTime start, LocalDateTime end);
}