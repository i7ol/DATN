package com.datn.shopapp.controller;

import com.datn.shopobject.dto.request.PaymentRequest;
import com.datn.shopobject.dto.response.PaymentResponse;
import com.datn.shoppayment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/payments")
@RequiredArgsConstructor
public class PaymentUserController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
            @RequestBody PaymentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        PaymentResponse response = paymentService.createPayment(request, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-payments")
    public ResponseEntity<List<PaymentResponse>> getMyPayments(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        List<PaymentResponse> payments = paymentService.getByUserId(userId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails) {
        PaymentResponse payment = paymentService.getByOrderId(orderId);
        // Kiểm tra ownership
        Long userId = Long.parseLong(userDetails.getUsername());
        if (!payment.getUserId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(payment);
    }

    @PutMapping("/{paymentId}/cancel")
    public ResponseEntity<PaymentResponse> cancelPayment(
            @PathVariable Long paymentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        PaymentResponse payment = paymentService.cancelPayment(paymentId);
        return ResponseEntity.ok(payment);
    }
}
