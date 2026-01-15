package com.datn.shopadmin.client;

import com.datn.shopobject.dto.response.PaymentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;


@Slf4j
public class PaymentAdminClientFallback implements PaymentAdminClient {

    @Override
    public Page<PaymentResponse> getAllPayments(
            String status,
            String method,
            LocalDate fromDate,
            LocalDate toDate,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            Pageable pageable) {

        log.error("Payment service unavailable - getAllPayments fallback");
        return Page.empty(pageable);
    }

    @Override
    public PaymentResponse getById(Long id) {
        log.error("Payment service unavailable - getById {}", id);
        return null;
    }

    @Override
    public PaymentResponse markPaid(Long id, String transactionId) {
        throw new IllegalStateException("Payment service is unavailable");
    }

    @Override
    public PaymentResponse refund(Long id, Map<String, String> request) {
        throw new IllegalStateException("Payment service is unavailable");
    }

    @Override
    public PaymentResponse cancel(Long id) {
        throw new IllegalStateException("Payment service is unavailable");
    }

    @Override
    public Object getSummary(LocalDate fromDate, LocalDate toDate) {
        log.error("Payment service unavailable - getSummary");
        return Map.of(
                "total", 0,
                "paid", 0,
                "refunded", 0
        );
    }
}

