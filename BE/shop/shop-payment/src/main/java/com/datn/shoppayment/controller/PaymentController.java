package com.datn.shoppayment.controller;

import com.datn.shopdatabase.enums.PaymentMethod;
import com.datn.shopdatabase.enums.PaymentStatus;
import com.datn.shopobject.dto.request.GuestPaymentRequest;
import com.datn.shopobject.dto.request.UserPaymentRequest;
import com.datn.shopobject.dto.response.PaymentResponse;
import com.datn.shoppayment.config.UserPrincipal;
import com.datn.shoppayment.service.PaymentService;

import com.datn.shoppayment.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/user")
    public ResponseEntity<PaymentResponse> userPayment(
            @Valid @RequestBody UserPaymentRequest request,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(
                paymentService.createUserPayment(
                        request,
                        principal.getId(),
                        httpRequest.getRemoteAddr()
                )
        );
    }

    @PostMapping("/guest")
    public ResponseEntity<PaymentResponse> guestPayment(
            @Valid @RequestBody GuestPaymentRequest request,
            HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(
                paymentService.createGuestPayment(
                        request,
                        httpRequest.getRemoteAddr()
                )
        );
    }

    @GetMapping
    public ResponseEntity<Page<PaymentResponse>> getAllPayments(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String method,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            Pageable pageable
    ) {
        PaymentStatus ps = status != null
                ? PaymentStatus.valueOf(status.toUpperCase())
                : null;

        PaymentMethod pm = method != null
                ? PaymentMethod.valueOf(method.toUpperCase())
                : null;

        return ResponseEntity.ok(
                paymentService.getAllPayments(
                        ps, pm, fromDate, toDate,
                        minAmount, maxAmount, pageable
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getById(id));
    }

    @PutMapping("/{id}/mark-paid")
    public ResponseEntity<PaymentResponse> markPaid(
            @PathVariable Long id,
            @RequestParam String transactionId) {

        return ResponseEntity.ok(
                paymentService.markPaid(id, transactionId)
        );
    }

    @PutMapping("/{id}/refund")
    public ResponseEntity<PaymentResponse> refund(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        return ResponseEntity.ok(
                paymentService.refund(id, request.get("reason"))
        );
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<PaymentResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.cancel(id));
    }

    @GetMapping("/summary")
    public ResponseEntity<Object> getSummary(
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate) {

        return ResponseEntity.ok(
                paymentService.getSummary(fromDate, toDate)
        );
    }
}


