//package com.datn.shoppayment.controller;
//
//import com.datn.shoppayment.dto.request.PaymentRequest;
//import com.datn.shoppayment.dto.request.UpdatePaymentStatusRequest;
//import com.datn.shoppayment.dto.response.PaymentResponse;
//import com.datn.shoppayment.service.PaymentService;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/payments")
//@RequiredArgsConstructor
//@CrossOrigin(origins = "*")
//public class PaymentController {
//
//    private final PaymentService paymentService;
//
//    @PostMapping("/create")
//    public ResponseEntity<PaymentResponse> createPayment(
//            @RequestBody PaymentRequest request
//    ) {
//        return ResponseEntity.ok(paymentService.createNewPayment(request));
//    }
//
//    @PutMapping("/update-status")
//    public ResponseEntity<PaymentResponse> updateStatus(
//            @RequestBody UpdatePaymentStatusRequest request
//    ) {
//        return ResponseEntity.ok(
//                paymentService.updatePaymentStatus(request.paymentId(), request.status())
//        );
//    }
//
//    @GetMapping("/{paymentId}")
//    public ResponseEntity<PaymentResponse> getPayment(@PathVariable Long paymentId) {
//        return ResponseEntity.ok(paymentService.getPaymentResponse(paymentId));
//    }
//
//    @PostMapping("/process")
//    public ResponseEntity<PaymentResponse> processPayment(
//            @RequestBody PaymentRequest request
//    ) {
//        return ResponseEntity.ok(paymentService.processPayment(request));
//    }
//}
