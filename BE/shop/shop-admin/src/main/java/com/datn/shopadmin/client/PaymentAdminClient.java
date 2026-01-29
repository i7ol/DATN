package com.datn.shopadmin.client;


import com.datn.shopdatabase.enums.PaymentMethod;
import com.datn.shopdatabase.enums.PaymentStatus;
import com.datn.shopobject.dto.response.PaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@FeignClient(
        name = "payment-service",
        url = "${payment.service.url}",
        fallback = PaymentAdminClientFallback.class
)
public interface PaymentAdminClient {

    @GetMapping("/api/payments")
    Page<PaymentResponse> getAllPayments(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String method,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            Pageable pageable
    );

    @GetMapping("/api/payments/{id}")
    PaymentResponse getById(@PathVariable Long id);

    @PutMapping("/api/payments/{id}/mark-paid")
    PaymentResponse markPaid(
            @PathVariable Long id,
            @RequestParam String transactionId
    );

    @PutMapping("/api/payments/{id}/refund")
    PaymentResponse refund(
            @PathVariable Long id,
            @RequestBody Map<String, String> request
    );

    @PutMapping("/api/payments/{id}/cancel")
    PaymentResponse cancel(@PathVariable Long id);

    @GetMapping("/api/payments/summary")
    Object getSummary(
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate
    );
}


