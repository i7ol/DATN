// PaymentWebhookController.java
package com.datn.shoppayment.controller;

import com.datn.shoppayment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/webhook/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentWebhookController {

    private final PaymentService paymentService;

    @PostMapping("/vnpay")
    public ResponseEntity<Void> handleVnpayWebhook(@RequestBody Map<String, String> payload) {
        try {
            String paymentId = payload.get("vnp_TxnRef");
            String transactionId = payload.get("vnp_TransactionNo");
            String responseCode = payload.get("vnp_ResponseCode");

            if ("00".equals(responseCode)) {
                paymentService.markPaid(Long.parseLong(paymentId), transactionId);
                log.info("VNPay webhook processed successfully for payment: {}", paymentId);
            } else {
                log.warn("VNPay payment failed for payment: {}", paymentId);
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error processing VNPay webhook", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/momo")
    public ResponseEntity<Void> handleMomoWebhook(@RequestBody Map<String, String> payload) {
        try {
            String paymentId = payload.get("orderId");
            String transactionId = payload.get("transId");
            String resultCode = payload.get("resultCode");

            if ("0".equals(resultCode)) {
                paymentService.markPaid(Long.parseLong(paymentId), transactionId);
                log.info("MoMo webhook processed successfully for payment: {}", paymentId);
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error processing MoMo webhook", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}