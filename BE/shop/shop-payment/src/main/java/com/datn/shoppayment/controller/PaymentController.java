package com.datn.shoppayment.controller;

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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
}


