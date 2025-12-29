package com.datn.shoppayment.service;

import com.datn.shopobject.dto.request.PaymentRequest;
import com.datn.shopobject.dto.request.PaymentSearchRequest;
import com.datn.shopobject.dto.response.PaymentResponse;
import com.datn.shopobject.dto.response.PaymentSummaryResponse;
import com.datn.shopobject.dto.response.VNPayPaymentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface PaymentService {

    // ===== USER FLOW =====
    PaymentResponse createPayment(PaymentRequest request, Long userId);

    VNPayPaymentResponse createVNPayPayment(
            Long orderId,
            Long amount,
            String orderInfo,
            String ipAddress
    );

    PaymentResponse getByOrderId(Long orderId);
    List<PaymentResponse> getByUserId(Long userId);

    // ===== SYSTEM (CALLBACK) =====
    PaymentResponse markPaidByOrderId(Long orderId, String transactionId);

    // Thêm các phương thức còn thiếu:
    PaymentResponse getById(Long paymentId);
    PaymentResponse cancelPayment(Long paymentId);
    PaymentResponse cancelPaymentByOrderId(Long orderId);

    // ===== ADMIN =====
    PaymentResponse markPaid(Long paymentId, String transactionId);

    PaymentResponse refund(Long paymentId, String refundReason);

    Page<PaymentResponse> getAllPayments(PaymentSearchRequest filter, Pageable pageable);

    PaymentSummaryResponse getPaymentSummary(LocalDate fromDate, LocalDate toDate);
}
