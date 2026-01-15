package com.datn.shopadmin.client;


import com.datn.shopobject.dto.response.PaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@FeignClient(
        name = "payment-admin-service",
        url = "${payment.service.url}",
        fallback = PaymentAdminClientFallback.class
)
public interface PaymentAdminClient {

    @GetMapping("/api/admin/payments")
    Page<PaymentResponse> getAllPayments(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String method,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            Pageable pageable
    );

    @GetMapping("/api/admin/payments/{id}")
    PaymentResponse getById(@PathVariable("id") Long id);

    @PutMapping("/api/admin/payments/{id}/mark-paid")
    PaymentResponse markPaid(
            @PathVariable Long id,
            @RequestParam String transactionId
    );

    @PutMapping("/api/admin/payments/{id}/refund")
    PaymentResponse refund(
            @PathVariable Long id,
            @RequestBody Map<String, String> request
    );

    @PutMapping("/api/admin/payments/{id}/cancel")
    PaymentResponse cancel(@PathVariable Long id);

    @GetMapping("/api/admin/payments/summary")
    Object getSummary(
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate
    );
}

