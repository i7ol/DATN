package com.datn.shoppayment.service;

import com.datn.shopdatabase.entity.PaymentEntity;
import com.datn.shopdatabase.enums.PaymentMethod;
import com.datn.shopdatabase.enums.PaymentStatus;
import com.datn.shopdatabase.repository.PaymentRepository;
import com.datn.shopobject.dto.request.PaymentRequest;
import com.datn.shopobject.dto.request.PaymentSearchRequest;
import com.datn.shopobject.dto.response.OrderResponse;
import com.datn.shopobject.dto.response.PaymentResponse;
import com.datn.shopobject.dto.response.PaymentSummaryResponse;
import com.datn.shopobject.dto.response.VNPayPaymentResponse;
import com.datn.shoppayment.client.OrderClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderClient orderClient;
    private final VNPayService vnPayService;

    // =================================================
    // USER FLOW
    // =================================================

    /**
     * Tạo payment cho order (chỉ tạo DB record)
     */
    @Override
    @Transactional
    public PaymentResponse createPayment(PaymentRequest request, Long userId) {

        PaymentMethod method = PaymentMethod.valueOf(request.getMethod().toUpperCase());
        if (method != PaymentMethod.VNPAY) {
            throw new RuntimeException("Only VNPAY is supported");
        }

        OrderResponse order = orderClient.getOrder(request.getOrderId());

        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Order does not belong to user");
        }

        // Không cho tạo payment trùng
        paymentRepository.findByOrderId(order.getId())
                .ifPresent(p -> {
                    throw new RuntimeException("Payment already exists for this order");
                });

        if (!"PENDING".equals(order.getPaymentStatus())) {
            throw new RuntimeException("Order is not eligible for payment");
        }

        PaymentEntity payment = PaymentEntity.builder()
                .orderId(order.getId())
                .userId(userId)
                .amount(order.getTotalPrice())
                .method(method)
                .status(PaymentStatus.PENDING)
                .build();

        paymentRepository.save(payment);

        log.info("Payment created for order {}", order.getId());
        return PaymentResponse.from(payment);
    }

    /**
     * Sinh URL thanh toán VNPay (KHÔNG update DB)
     */
    @Override
    public VNPayPaymentResponse createVNPayPayment(
            Long orderId,
            Long amount,
            String orderInfo,
            String ipAddress
    ) {
        PaymentEntity payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new RuntimeException("Payment is not pending");
        }

        return vnPayService.createPayment(orderId, amount, orderInfo, ipAddress);
    }

    // =================================================
    // CALLBACK / SYSTEM FLOW
    // =================================================

    /**
     * Được gọi từ VNPay callback
     */
    @Override
    @Transactional
    public PaymentResponse markPaidByOrderId(Long orderId, String transactionId) {

        PaymentEntity payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.getStatus() == PaymentStatus.PAID) {
            return PaymentResponse.from(payment); // idempotent
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new RuntimeException("Invalid payment state");
        }

        payment.setStatus(PaymentStatus.PAID);
        payment.setTransactionId(transactionId);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // Đồng bộ sang Order Service
        orderClient.updateOrderPayment(orderId, "PAID");

        log.info("Payment marked as PAID for order {}", orderId);
        return PaymentResponse.from(payment);
    }
    @Override
    public PaymentResponse getById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .map(PaymentResponse::from)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
    }

    @Override
    @Transactional
    public PaymentResponse cancelPayment(Long paymentId) {
        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new RuntimeException("Only PENDING payments can be cancelled");
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        payment.setCancelledAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // Đồng bộ sang Order Service
        orderClient.updateOrderPayment(payment.getOrderId(), "CANCELLED");

        log.info("Payment cancelled: {}", paymentId);
        return PaymentResponse.from(payment);
    }

    @Override
    @Transactional
    public PaymentResponse cancelPaymentByOrderId(Long orderId) {
        PaymentEntity payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new RuntimeException("Only PENDING payments can be cancelled");
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        payment.setCancelledAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // Đồng bộ sang Order Service
        orderClient.updateOrderPayment(orderId, "CANCELLED");

        log.info("Payment cancelled for order: {}", orderId);
        return PaymentResponse.from(payment);
    }

    // =================================================
    // ADMIN FLOW
    // =================================================

    @Override
    @Transactional
    public PaymentResponse refund(Long paymentId, String reason) {
        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.PAID) {
            throw new RuntimeException("Only PAID payments can be refunded");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefundReason(reason);
        payment.setRefundedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        orderClient.updateOrderPayment(payment.getOrderId(), "REFUNDED");

        return PaymentResponse.from(payment);
    }

    // =================================================
    // QUERY
    // =================================================

    @Override
    public PaymentResponse getByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .map(PaymentResponse::from)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
    }

    @Override
    public List<PaymentResponse> getByUserId(Long userId) {
        return paymentRepository.findByUserId(userId)
                .stream()
                .map(PaymentResponse::from)
                .toList();
    }

    @Override
    public Page<PaymentResponse> getAllPayments(PaymentSearchRequest filter, Pageable pageable) {

        LocalDateTime from = filter.getFromDate() != null
                ? filter.getFromDate().atStartOfDay()
                : null;

        LocalDateTime to = filter.getToDate() != null
                ? filter.getToDate().atTime(LocalTime.MAX)
                : null;

        PaymentStatus status = filter.getStatus() != null
                ? PaymentStatus.valueOf(filter.getStatus().toUpperCase())
                : null;

        PaymentMethod method = filter.getMethod() != null
                ? PaymentMethod.valueOf(filter.getMethod().toUpperCase())
                : null;

        return paymentRepository.findAllWithFilters(
                status,
                method,
                from,
                to,
                filter.getMinAmount(),
                filter.getMaxAmount(),
                pageable
        ).map(PaymentResponse::from);
    }

    @Override
    public PaymentSummaryResponse getPaymentSummary(LocalDate from, LocalDate to) {
        BigDecimal paid = paymentRepository.getTotalAmountByPeriodAndStatus(
                from, to, PaymentStatus.PAID
        );
        BigDecimal pending = paymentRepository.getTotalAmountByPeriodAndStatus(
                from, to, PaymentStatus.PENDING
        );

        return PaymentSummaryResponse.builder()
                .totalAmount(paid != null ? paid : BigDecimal.ZERO)
                .pendingAmount(pending != null ? pending : BigDecimal.ZERO)
                .build();
    }

    @Override
    @Transactional
    public PaymentResponse markPaid(Long paymentId, String transactionId) {
        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new RuntimeException("Only PENDING payments can be marked as PAID");
        }

        payment.setStatus(PaymentStatus.PAID);
        payment.setTransactionId(transactionId);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // Đồng bộ sang Order Service
        orderClient.updateOrderPayment(payment.getOrderId(), "PAID");

        log.info("Payment marked as PAID: {}", paymentId);
        return PaymentResponse.from(payment);
    }

}
