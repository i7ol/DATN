package com.datn.shoppayment.service;

import com.datn.shopdatabase.entity.PaymentEntity;
import com.datn.shopdatabase.enums.PaymentMethod;
import com.datn.shopdatabase.enums.PaymentStatus;
import com.datn.shoppayment.client.OrderClient;
import com.datn.shopobject.dto.request.PaymentRequest;
import com.datn.shopobject.dto.request.PaymentSearchRequest;
import com.datn.shopobject.dto.response.OrderResponse;
import com.datn.shopobject.dto.response.PaymentResponse;
import com.datn.shopobject.dto.response.PaymentSummaryResponse;
import com.datn.shopdatabase.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderClient orderClient;

    // USER tạo payment
    @Transactional
    public PaymentResponse createPayment(PaymentRequest req, Long userId) {
        OrderResponse order = orderClient.getOrder(req.orderId());

        // Kiểm tra order thuộc về user
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Order does not belong to user");
        }

        // Kiểm tra order đã có payment chưa
        Optional<PaymentEntity> existingPayment = paymentRepository.findByOrderId(req.orderId());
        if (existingPayment.isPresent()) {
            throw new RuntimeException("Order already has a payment");
        }

        // Kiểm tra trạng thái order
        if (!"PENDING_PAYMENT".equals(order.getPaymentStatus()) && !"PENDING".equals(order.getPaymentStatus())) {
            throw new RuntimeException("Order status is not valid for payment");
        }

        PaymentEntity payment = PaymentEntity.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .amount(order.getTotalPrice())
                .method(PaymentMethod.valueOf(req.method()))
                .status(PaymentStatus.PENDING)
                .transactionId(generateTransactionId())
                .createdAt(LocalDateTime.now())
                .build();

        paymentRepository.save(payment);

        // Cập nhật trạng thái order payment
        orderClient.updateOrderPayment(payment.getOrderId(), "PENDING");

        return PaymentResponse.from(payment);
    }

    // WEBHOOK / SYSTEM update
    @Transactional
    public PaymentResponse markPaid(Long paymentId, String transactionId) {
        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new RuntimeException("Payment is not in PENDING status");
        }

        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        payment.setTransactionId(transactionId);
        paymentRepository.save(payment);

        orderClient.updateOrderPayment(payment.getOrderId(), "PAID");

        return PaymentResponse.from(payment);
    }

    // ADMIN refund
    @Transactional
    public PaymentResponse refund(Long paymentId, String refundReason) {
        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.PAID) {
            throw new RuntimeException("Only paid payments can be refunded");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefundedAt(LocalDateTime.now());
        payment.setRefundReason(refundReason);
        paymentRepository.save(payment);

        orderClient.updateOrderPayment(payment.getOrderId(), "REFUNDED");

        return PaymentResponse.from(payment);
    }

    // Cancel payment
    @Transactional
    public PaymentResponse cancelPayment(Long paymentId) {
        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new RuntimeException("Only pending payments can be cancelled");
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        payment.setCancelledAt(LocalDateTime.now());
        paymentRepository.save(payment);

        orderClient.updateOrderPayment(payment.getOrderId(), "CANCELLED");

        return PaymentResponse.from(payment);
    }

    // Get payment by ID
    public PaymentResponse getById(Long id) {
        PaymentEntity payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        return PaymentResponse.from(payment);
    }

    // Get payment by order ID
    public PaymentResponse getByOrderId(Long orderId) {
        PaymentEntity payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        return PaymentResponse.from(payment);
    }

    // Get payments by user ID
    public List<PaymentResponse> getByUserId(Long userId) {
        List<PaymentEntity> payments = paymentRepository.findByUserId(userId);
        return payments.stream()
                .map(PaymentResponse::from)
                .toList();
    }

    // Get all payments with filtering
    public Page<PaymentResponse> getAllPayments(PaymentSearchRequest filter, Pageable pageable) {
        // Convert LocalDate to LocalDateTime for query
        LocalDateTime fromDateTime = filter.getFromDate() != null ?
                filter.getFromDate().atStartOfDay() : null;
        LocalDateTime toDateTime = filter.getToDate() != null ?
                filter.getToDate().atTime(LocalTime.MAX) : null;

        return paymentRepository.findAllWithFilters(
                filter.getStatus(),
                filter.getMethod(),
                fromDateTime,
                toDateTime,
                filter.getMinAmount(),
                filter.getMaxAmount(),
                pageable
        ).map(PaymentResponse::from);
    }

    // Get payment summary
    public PaymentSummaryResponse getPaymentSummary(LocalDate fromDate, LocalDate toDate) {
        LocalDateTime startDateTime = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime endDateTime = toDate != null ? toDate.atTime(LocalTime.MAX) : null;

        BigDecimal totalAmount = paymentRepository.getTotalAmountByPeriodAndStatus(
                fromDate, toDate, PaymentStatus.PAID);
        BigDecimal pendingAmount = paymentRepository.getTotalAmountByPeriodAndStatus(
                fromDate, toDate, PaymentStatus.PENDING);

        long totalCount = 0;
        long paidCount = 0;

        if (startDateTime != null && endDateTime != null) {
            totalCount = paymentRepository.countByCreatedAtBetween(startDateTime, endDateTime);
            paidCount = paymentRepository.countByStatusAndCreatedAtBetween(
                    PaymentStatus.PAID, startDateTime, endDateTime);
        }

        return PaymentSummaryResponse.builder()
                .totalAmount(totalAmount != null ? totalAmount : BigDecimal.ZERO)
                .pendingAmount(pendingAmount != null ? pendingAmount : BigDecimal.ZERO)
                .totalTransactions(totalCount)
                .successfulTransactions(paidCount)
                .build();
    }

    // Generate transaction ID
    private String generateTransactionId() {
        return "TXN" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }
}