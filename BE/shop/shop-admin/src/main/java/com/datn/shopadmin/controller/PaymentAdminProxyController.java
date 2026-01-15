package com.datn.shopadmin.controller;

import com.datn.shopadmin.client.PaymentAdminClient;
import com.datn.shopobject.dto.request.PaymentSearchRequest;
import com.datn.shopobject.dto.response.PaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
public class PaymentAdminProxyController {

    private final PaymentAdminClient paymentClient;

    // 1. Lấy danh sách payment (có filter)
    @GetMapping
    public ResponseEntity<Page<PaymentResponse>> getAllPayments(
            PaymentSearchRequest filter,
            Pageable pageable) {

        return ResponseEntity.ok(
                paymentClient.getAllPayments(
                        filter.getStatus(),
                        filter.getMethod(),
                        filter.getFromDate(),
                        filter.getToDate(),
                        filter.getMinAmount(),
                        filter.getMaxAmount(),
                        pageable
                )
        );
    }

    // 2. Lấy payment theo ID
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentClient.getById(id));
    }

    // 3. Đánh dấu đã thanh toán
    @PutMapping("/{id}/mark-paid")
    public ResponseEntity<PaymentResponse> markPaid(
            @PathVariable Long id,
            @RequestParam String transactionId) {

        return ResponseEntity.ok(
                paymentClient.markPaid(id, transactionId)
        );
    }

    // 4. Hoàn tiền
    @PutMapping("/{id}/refund")
    public ResponseEntity<PaymentResponse> refund(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        return ResponseEntity.ok(
                paymentClient.refund(id, request)
        );
    }

    // 5. Huỷ payment
    @PutMapping("/{id}/cancel")
    public ResponseEntity<PaymentResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(paymentClient.cancel(id));
    }

    // 6. Tổng quan thanh toán
    @GetMapping("/summary")
    public ResponseEntity<Object> getSummary(
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate) {

        return ResponseEntity.ok(
                paymentClient.getSummary(fromDate, toDate)
        );
    }
}
