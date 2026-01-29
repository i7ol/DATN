package com.datn.shopdatabase.repository;

import com.datn.shopdatabase.entity.OrderEntity;
import com.datn.shopdatabase.enums.OrderStatus;
import com.datn.shopdatabase.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    // ==================== BASIC QUERIES ====================
    Page<OrderEntity> findByUserId(Long userId, Pageable pageable);
    List<OrderEntity> findByUserIdIsNull();
    List<OrderEntity> findByStatus(OrderStatus status);
    List<OrderEntity> findByPaymentStatus(PaymentStatus paymentStatus);
    List<OrderEntity> findByCreatedAtBetween(Instant startDate, Instant endDate);

    // ==================== COUNT QUERIES ====================
    long countByStatus(OrderStatus status);
    long countByUserId(Long userId);

    // ==================== FIND WITH PAGINATION ====================
    List<OrderEntity> findByOrderByCreatedAtDesc(Pageable pageable);

    default List<OrderEntity> findRecentOrders(int limit) {
        return findByOrderByCreatedAtDesc(Pageable.ofSize(limit));
    }

    List<OrderEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    default List<OrderEntity> findUserRecentOrders(Long userId, int limit) {
        return findByUserIdOrderByCreatedAtDesc(userId, Pageable.ofSize(limit));
    }

    // ==================== COMBINATION QUERIES ====================
    List<OrderEntity> findByUserIdAndStatus(Long userId, OrderStatus status);
    List<OrderEntity> findByUserIdAndPaymentStatus(Long userId, PaymentStatus paymentStatus);
    List<OrderEntity> findByCreatedAtBetweenAndStatus(Instant startDate, Instant endDate, OrderStatus status);

    // ==================== BUSINESS QUERIES ====================
    List<OrderEntity> findByTotalPriceGreaterThan(BigDecimal minPrice);
    List<OrderEntity> findByFinalAmountGreaterThanEqual(BigDecimal amount);

    // ==================== JPQL QUERIES (SIMPLE) ====================

    /**
     * Tìm đơn hàng theo khoảng ngày
     */
    @Query("SELECT o FROM OrderEntity o WHERE o.createdAt >= :startDate AND o.createdAt <= :endDate " +
            "ORDER BY o.createdAt DESC")
    List<OrderEntity> findOrdersByDateRange(@Param("startDate") Instant startDate,
                                            @Param("endDate") Instant endDate);

    /**
     * Top sản phẩm bán chạy
     */
    @Query("SELECT oi.productId, oi.productName, SUM(oi.quantity) as totalQuantity " +
            "FROM OrderItemEntity oi " +
            "WHERE oi.order.status = com.datn.shopdatabase.enums.OrderStatus.COMPLETED " +
            "GROUP BY oi.productId, oi.productName " +
            "ORDER BY totalQuantity DESC")
    List<Object[]> findTopSellingProducts(Pageable pageable);

    default List<Object[]> findTopSellingProducts(int limit) {
        return findTopSellingProducts(Pageable.ofSize(limit));
    }

    /**
     * Đơn hàng quá hạn giao
     */
    @Query("SELECT o FROM OrderEntity o " +
            "WHERE o.estimatedDeliveryDate < :currentTime " +
            "AND o.status IN (com.datn.shopdatabase.enums.OrderStatus.PROCESSING, " +
            "com.datn.shopdatabase.enums.OrderStatus.SHIPPING) " +
            "ORDER BY o.estimatedDeliveryDate ASC")
    List<OrderEntity> findOverdueOrders(@Param("currentTime") Instant currentTime);

    /**
     * Doanh thu theo user
     */
    @Query("SELECT o.userId, COUNT(o), SUM(o.finalAmount) " +
            "FROM OrderEntity o " +
            "WHERE o.userId IS NOT NULL AND o.status = com.datn.shopdatabase.enums.OrderStatus.COMPLETED " +
            "GROUP BY o.userId " +
            "ORDER BY SUM(o.finalAmount) DESC")
    List<Object[]> findTopCustomersByRevenue(Pageable pageable);

    /**
     * Đơn hàng hôm nay - SỬA LẠI: dùng Instant range
     */
    @Query("SELECT o FROM OrderEntity o " +
            "WHERE o.createdAt >= :startOfDay AND o.createdAt <= :endOfDay " +
            "ORDER BY o.createdAt DESC")
    List<OrderEntity> findTodayOrders(@Param("startOfDay") Instant startOfDay,
                                      @Param("endOfDay") Instant endOfDay);

    default List<OrderEntity> findTodayOrders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = now.toLocalDate().atTime(23, 59, 59, 999999999);

        Instant startInstant = startOfDay.atZone(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endOfDay.atZone(ZoneId.systemDefault()).toInstant();

        return findTodayOrders(startInstant, endInstant);
    }

    /**
     * Đơn hàng theo tháng - SỬA LẠI: dùng Instant range
     */
    @Query("SELECT o FROM OrderEntity o " +
            "WHERE o.createdAt >= :startOfMonth AND o.createdAt <= :endOfMonth " +
            "ORDER BY o.createdAt DESC")
    List<OrderEntity> findOrdersByMonth(@Param("startOfMonth") Instant startOfMonth,
                                        @Param("endOfMonth") Instant endOfMonth);

    /**
     * Thống kê đơn hàng
     */
    @Query("SELECT COUNT(o), SUM(o.finalAmount), AVG(o.finalAmount) " +
            "FROM OrderEntity o " +
            "WHERE o.status = com.datn.shopdatabase.enums.OrderStatus.COMPLETED " +
            "AND o.createdAt BETWEEN :startDate AND :endDate")
    Object[] getOrderStatistics(@Param("startDate") Instant startDate,
                                @Param("endDate") Instant endDate);

    /**
     * Tìm đơn hàng có sản phẩm cụ thể
     */
    @Query("SELECT DISTINCT o FROM OrderEntity o JOIN o.items i WHERE i.productId = :productId")
    List<OrderEntity> findOrdersByProductId(@Param("productId") Long productId);

    /**
     * Tìm đơn hàng theo tên sản phẩm
     */
    @Query("SELECT DISTINCT o FROM OrderEntity o JOIN o.items i " +
            "WHERE LOWER(i.productName) LIKE LOWER(CONCAT('%', :productName, '%'))")
    List<OrderEntity> findOrdersByProductName(@Param("productName") String productName);

    /**
     * Tìm đơn hàng theo khách hàng
     */
    @Query("SELECT o FROM OrderEntity o " +
            "WHERE (o.userId = :userId OR LOWER(o.guestName) LIKE LOWER(CONCAT('%', :customerName, '%')) " +
            "OR LOWER(o.guestEmail) LIKE LOWER(CONCAT('%', :customerName, '%')))")
    List<OrderEntity> findOrdersByCustomer(@Param("userId") Long userId,
                                           @Param("customerName") String customerName);

    // ==================== NATIVE QUERIES (ORACLE SPECIFIC) ====================
    // Tách ra class riêng nếu cần phức tạp

    /**
     * Thống kê theo ngày - Native Query (nếu cần)
     */
    @Query(value = """
        SELECT TRUNC(o.created_at) as stat_date, 
               COUNT(*) as order_count, 
               COALESCE(SUM(o.final_amount), 0) as total_revenue 
        FROM orders o 
        WHERE o.created_at BETWEEN :startDate AND :endDate 
        GROUP BY TRUNC(o.created_at) 
        ORDER BY stat_date
        """, nativeQuery = true)
    List<Object[]> getDailyStatisticsNative(@Param("startDate") java.sql.Timestamp startDate,
                                            @Param("endDate") java.sql.Timestamp endDate);

    /**
     * Top khách hàng - Native Query
     */
    @Query(value = """
        SELECT o.user_id, 
               COUNT(*) as order_count, 
               COALESCE(SUM(o.final_amount), 0) as total_spent 
        FROM orders o 
        WHERE o.user_id IS NOT NULL 
        AND o.status = 'COMPLETED' 
        GROUP BY o.user_id 
        ORDER BY total_spent DESC 
        FETCH FIRST :limit ROWS ONLY
        """, nativeQuery = true)
    List<Object[]> getTopCustomersNative(@Param("limit") int limit);
}