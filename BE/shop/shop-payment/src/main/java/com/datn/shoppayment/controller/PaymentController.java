package com.datn.shoppayment.controller;

import com.datn.shoppayment.dto.request.PaymentRequest;
import com.datn.shoppayment.dto.response.PaymentResponse;
import com.datn.shoppayment.entity.Payment;
import com.datn.shoppayment.enums.PaymentMethod;
import com.datn.shoppayment.enums.PaymentStatus;
import com.datn.shoppayment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create")
    public PaymentResponse createPayment(@RequestParam(name="orderId") Long orderId,
                                         @RequestParam(name="method") PaymentMethod method) {
        Payment p = paymentService.createPayment(orderId, method);
        return new PaymentResponse(p.getId(), p.getOrderId(), p.getStatus());
    }

    @PutMapping("/update-status")
    public PaymentResponse updateStatus(@RequestParam(name="paymentId") Long paymentId,
                                        @RequestParam(name="status") PaymentStatus status) {
        Payment p = paymentService.updatePaymentStatus(paymentId, status);
        return new PaymentResponse(p.getId(), p.getOrderId(), p.getStatus());
    }

    @GetMapping("/{paymentId}")
    public PaymentResponse getPayment(@PathVariable("paymentId") Long paymentId) {
        Payment p = paymentService.getPayment(paymentId);
        return new PaymentResponse(p.getId(), p.getOrderId(), p.getStatus());
    }

    @PostMapping("/process")
    public PaymentResponse processPayment(@RequestBody PaymentRequest request) {
        return paymentService.processPayment(request);
    }
}
