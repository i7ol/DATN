package com.datn.shoppayment.service;

import com.datn.shopdatabase.enums.PaymentMethod;
import com.datn.shopdatabase.enums.PaymentStatus;
import com.datn.shopobject.dto.request.GuestPaymentRequest;
import com.datn.shopobject.dto.request.UserPaymentRequest;
import com.datn.shopobject.dto.response.PaymentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface PaymentService {

    PaymentResponse createUserPayment(
            UserPaymentRequest request,
            Long userId,
            String ipAddress
    );

    PaymentResponse createGuestPayment(
            GuestPaymentRequest request,
            String ipAddress
    );

    Page<PaymentResponse> getAllPayments(
            PaymentStatus status,
            PaymentMethod method,
            LocalDate fromDate,
            LocalDate toDate,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            Pageable pageable
    );

    PaymentResponse getById(Long id);

    PaymentResponse markPaid(Long id, String transactionId);

    PaymentResponse refund(Long id, String reason);

    PaymentResponse cancel(Long id);

    Object getSummary(LocalDate fromDate, LocalDate toDate);
}
