package com.datn.shopadmin.controller;


import com.datn.shopobject.dto.request.PaymentSearchRequest;
import com.datn.shopobject.dto.response.PaymentResponse;
import com.datn.shopobject.dto.response.PaymentSummaryResponse;
import com.datn.shoppayment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
public class PaymentAdminController {

    private final PaymentService paymentService;

    @GetMapping
    public ResponseEntity<Page<PaymentResponse>> getAllPayments(
            PaymentSearchRequest filter,
            Pageable pageable) {
        Page<PaymentResponse> payments = paymentService.getAllPayments(filter, pageable);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/summary")
    public ResponseEntity<PaymentSummaryResponse> getPaymentSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        if (fromDate == null) fromDate = LocalDate.now().minusMonths(1);
        if (toDate == null) toDate = LocalDate.now();
        PaymentSummaryResponse summary = paymentService.getPaymentSummary(fromDate, toDate);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Long id) {
        PaymentResponse payment = paymentService.getById(id);
        return ResponseEntity.ok(payment);
    }

    @PutMapping("/{id}/mark-paid")
    public ResponseEntity<PaymentResponse> markPaid(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String transactionId = request.get("transactionId");
        PaymentResponse payment = paymentService.markPaid(id, transactionId);
        return ResponseEntity.ok(payment);
    }

    @PutMapping("/{id}/refund")
    public ResponseEntity<PaymentResponse> refund(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String reason = request.get("reason");
        PaymentResponse payment = paymentService.refund(id, reason);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(@PathVariable Long orderId) {
        PaymentResponse payment = paymentService.getByOrderId(orderId);
        return ResponseEntity.ok(payment);
    }
}

