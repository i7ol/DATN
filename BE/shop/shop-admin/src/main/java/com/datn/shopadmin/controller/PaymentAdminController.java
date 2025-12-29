package com.datn.shopadmin.controller;

import com.datn.shopobject.dto.request.PaymentSearchRequest;
import com.datn.shopobject.dto.response.PaymentResponse;
import com.datn.shoppayment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
public class PaymentAdminController {

    private final PaymentService paymentService;

    @GetMapping
    public ResponseEntity<?> getAllPayments(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String method,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            Pageable pageable) {

        PaymentSearchRequest filter = PaymentSearchRequest.builder()
                .status(status)
                .method(method)
                .fromDate(fromDate)
                .toDate(toDate)
                .minAmount(minAmount)
                .maxAmount(maxAmount)
                .build();

        Page<PaymentResponse> payments = paymentService.getAllPayments(filter, pageable);
        return ResponseEntity.ok(payments);
    }


    // 2. Lấy payment theo ID
    @GetMapping("/{paymentId}")
    public ResponseEntity<?> getPaymentById(@PathVariable Long paymentId) {
        try {
            PaymentResponse payment = paymentService.getById(paymentId);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    // 3. Đánh dấu đã thanh toán
    @PutMapping("/{paymentId}/mark-paid")
    public ResponseEntity<?> markPaid(
            @PathVariable Long paymentId,
            @RequestParam String transactionId) {
        try {
            PaymentResponse payment = paymentService.markPaid(paymentId, transactionId);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 4. Hoàn tiền - CHỈ GIỮ MỘT PHƯƠNG THỨC
    @PutMapping("/{paymentId}/refund")
    public ResponseEntity<?> refund(
            @PathVariable Long paymentId,
            @RequestBody Map<String, String> request) {
        try {
            String refundReason = request.get("refundReason");
            PaymentResponse payment = paymentService.refund(paymentId, refundReason);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 5. Hủy payment
    @PutMapping("/{paymentId}/cancel")
    public ResponseEntity<?> cancelPayment(@PathVariable Long paymentId) {
        try {
            PaymentResponse payment = paymentService.cancelPayment(paymentId);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 6. Lấy tổng quan thanh toán
    @GetMapping("/summary")
    public ResponseEntity<?> getPaymentSummary(
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate) {

        var summary = paymentService.getPaymentSummary(fromDate, toDate);
        return ResponseEntity.ok(summary);
    }
}