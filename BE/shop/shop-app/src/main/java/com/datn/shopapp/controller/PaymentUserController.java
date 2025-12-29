package com.datn.shopapp.controller;

import com.datn.shopobject.dto.request.PaymentRequest;
import com.datn.shopobject.dto.response.PaymentResponse;
import com.datn.shopobject.dto.response.VNPayPaymentResponse;
import com.datn.shoppayment.service.PaymentService;
import com.datn.shoppayment.service.VNPayService;
import com.datn.shopapp.client.CartClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentUserController {

    private final PaymentService paymentService;
    private final VNPayService vnPayService;
    private final CartClient cartClient;

    // ================= User Endpoints =================

    @PostMapping
    public ResponseEntity<?> createPayment(
            @RequestBody PaymentRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpRequest) {

        Long userId = Long.parseLong(userDetails.getUsername());
        log.info("Creating payment for user {}: {}", userId, request);

        try {
            PaymentResponse response = paymentService.createPayment(request, userId);

            if ("VNPAY".equalsIgnoreCase(request.getMethod())) {
                try {
                    String ipAddress = getClientIpAddress(httpRequest);
                    String orderInfo = "Thanh toán đơn hàng #" + request.getOrderId();

                    VNPayPaymentResponse vnpayResponse = paymentService.createVNPayPayment(
                            request.getOrderId(),
                            response.getAmount().longValue(),
                            orderInfo,
                            ipAddress
                    );

                    response.setPaymentUrl(vnpayResponse.getPaymentUrl());
                    response.setQrCodeUrl(vnpayResponse.getQrCodeUrl());
                    response.setTransactionId(vnpayResponse.getTransactionId());

                } catch (Exception e) {
                    log.error("Error creating VNPay URL: {}", e.getMessage());
                }
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error creating payment: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Cannot create payment: " + e.getMessage()));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyPayments(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        try {
            List<PaymentResponse> payments = paymentService.getByUserId(userId);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            log.error("Error getting payments: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "Cannot retrieve payments"));
        }
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<?> getPaymentById(
            @PathVariable Long paymentId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = Long.parseLong(userDetails.getUsername());
        try {
            PaymentResponse payment = paymentService.getById(paymentId);
            if (!payment.getUserId().equals(userId)) return ResponseEntity.status(403).build();
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            log.error("Error getting payment by id: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getPaymentByOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = Long.parseLong(userDetails.getUsername());
        try {
            PaymentResponse payment = paymentService.getByOrderId(orderId);
            if (!payment.getUserId().equals(userId)) return ResponseEntity.status(403).build();
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            log.error("Error getting payment by order: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{paymentId}/cancel")
    public ResponseEntity<?> cancelPayment(
            @PathVariable Long paymentId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = Long.parseLong(userDetails.getUsername());
        try {
            PaymentResponse payment = paymentService.getById(paymentId);
            if (!payment.getUserId().equals(userId)) return ResponseEntity.status(403).build();

            PaymentResponse cancelled = paymentService.cancelPayment(paymentId);
            return ResponseEntity.ok(cancelled);
        } catch (Exception e) {
            log.error("Error cancelling payment: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    // ================= Internal Endpoints =================

    @PutMapping("/order/{orderId}/mark-paid")
    public ResponseEntity<?> markPaidByOrderId(
            @PathVariable Long orderId,
            @RequestParam String transactionId) {

        try {
            PaymentResponse payment = paymentService.markPaidByOrderId(orderId, transactionId);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            log.error("Error marking payment as PAID: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ================= VNPay Callback =================

    @GetMapping("/vnpay/return")
    public ResponseEntity<?> vnpayPaymentReturn(HttpServletRequest request) {
        log.info("Received VNPAY payment return callback");

        try {
            int paymentStatus = vnPayService.orderReturn(request);

            String orderInfo = request.getParameter("vnp_OrderInfo");
            String paymentTime = request.getParameter("vnp_PayDate");
            String transactionId = request.getParameter("vnp_TransactionNo");
            String totalPrice = request.getParameter("vnp_Amount");
            String orderIdStr = request.getParameter("vnp_TxnRef");

            Long orderId = tryExtractOrderId(orderInfo, orderIdStr);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("orderInfo", orderInfo);
            responseData.put("paymentTime", paymentTime);
            responseData.put("transactionId", transactionId);
            responseData.put("totalPrice", totalPrice);

            if (paymentStatus == 1 && orderId != null) {
                try {
                    PaymentResponse paymentResponse = paymentService.markPaidByOrderId(orderId, transactionId);
                    clearCartAfterPayment(paymentResponse.getUserId(), orderId);

                    responseData.put("success", true);
                    responseData.put("orderId", orderId);
                    responseData.put("paymentId", paymentResponse.getId());
                    responseData.put("message", "Thanh toán thành công");

                } catch (Exception e) {
                    log.error("Error processing payment: {}", e.getMessage(), e);
                    responseData.put("success", true);
                    responseData.put("message", "Thanh toán thành công trên VNPay");
                }
            } else if (paymentStatus == 0 && orderId != null) {
                try {
                    paymentService.cancelPaymentByOrderId(orderId);
                } catch (Exception e) {
                    log.error("Error cancelling payment: {}", e.getMessage(), e);
                }
                responseData.put("success", false);
                responseData.put("message", "Thanh toán thất bại");
            } else {
                responseData.put("success", false);
                responseData.put("message", "Chữ ký không hợp lệ");
            }

            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", buildRedirectUrl(responseData))
                    .build();

        } catch (Exception e) {
            log.error("Error processing VNPAY callback", e);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", "http://localhost:4200/payment/result?success=false&message=System+error")
                    .build();
        }
    }

    // ================= Helper Methods =================

    private Long tryExtractOrderId(String orderInfo, String vnpTxnRef) {
        try {
            if (orderInfo != null && orderInfo.contains("#")) {
                String[] parts = orderInfo.split("#");
                if (parts.length > 1) return Long.parseLong(parts[1].trim());
            }
            if (vnpTxnRef != null && !vnpTxnRef.isEmpty())
                return Long.parseLong(vnpTxnRef.replaceFirst("^0+(?!$)", ""));
        } catch (NumberFormatException e) {
            log.warn("Cannot extract orderId from orderInfo: {} or vnpTxnRef: {}", orderInfo, vnpTxnRef);
        }
        return null;
    }

    private void clearCartAfterPayment(Long userId, Long orderId) {
        try {
            cartClient.clearCart(userId);
            log.info("Cart cleared for user {} after order {}", userId, orderId);
        } catch (Exception e) {
            log.error("Error clearing cart for user {}: {}", userId, e.getMessage());
        }
    }

    private String buildRedirectUrl(Map<String, Object> data) {
        StringBuilder url = new StringBuilder("http://localhost:4200/payment/result?");
        data.forEach((k, v) -> {
            if (v != null) url.append(k).append("=").append(v.toString()).append("&");
        });
        return url.toString();
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }
        return ipAddress != null ? ipAddress : "127.0.0.1";
    }
}
