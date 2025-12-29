//package com.datn.shopapp.controller;
//
//import com.datn.shoppayment.service.PaymentService;
//import com.datn.shoppayment.service.VNPayService;
//import com.datn.shopapp.client.CartClient;
//import jakarta.servlet.http.HttpServletRequest;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Enumeration;
//import java.util.HashMap;
//import java.util.Map;
//
//@Slf4j
//@RestController
//@RequestMapping("/api/payment")
//@RequiredArgsConstructor
//public class PaymentCallbackController {
//
//    private final VNPayService vnPayService;
//    private final PaymentService paymentService;
//    private final CartClient cartClient;
//
//    /**
//     * Xử lý callback từ VNPay (giống mẫu)
//     */
//    @GetMapping("/vnpay-payment")
//    public ResponseEntity<?> vnpayPaymentReturn(HttpServletRequest request) {
//        log.info("Received VNPAY payment return callback");
//
//        try {
//            // Xử lý kết quả trả về từ VNPay
//            int paymentStatus = vnPayService.orderReturn(request);
//
//            // Lấy thông tin từ request
//            String orderInfo = request.getParameter("vnp_OrderInfo");
//            String paymentTime = request.getParameter("vnp_PayDate");
//            String transactionId = request.getParameter("vnp_TransactionNo");
//            String totalPrice = request.getParameter("vnp_Amount");
//            String orderIdStr = request.getParameter("vnp_TxnRef");
//
//            log.info("VNPAY return - Status: {}, Order: {}, Transaction: {}",
//                    paymentStatus, orderInfo, transactionId);
//
//            // Chuẩn bị response data
//            Map<String, Object> responseData = new HashMap<>();
//            responseData.put("orderInfo", orderInfo);
//            responseData.put("paymentTime", paymentTime);
//            responseData.put("transactionId", transactionId);
//            responseData.put("totalPrice", totalPrice);
//
//            if (paymentStatus == 1) {
//                // Thanh toán thành công
//                // Cố gắng extract orderId từ orderInfo (nếu có)
//                Long orderId = tryExtractOrderId(orderInfo, orderIdStr);
//
//                if (orderId != null) {
//                    try {
//                        // Cập nhật trạng thái payment thành PAID
//                        var paymentResponse = paymentService.markPaidByOrderId(orderId, transactionId);
//                        log.info("Payment marked as PAID for order: {}, paymentId: {}", orderId, paymentResponse.getId());
//
//                        // CLEAR CART ITEMS
//                        clearCartAfterPayment(paymentResponse.getUserId(), orderId);
//
//                        responseData.put("success", true);
//                        responseData.put("orderId", orderId);
//                        responseData.put("paymentId", paymentResponse.getId());
//                        responseData.put("message", "Thanh toán thành công");
//                    } catch (Exception e) {
//                        log.error("Error processing payment: {}", e.getMessage(), e);
//                        responseData.put("success", true); // VNPay đã thanh toán thành công
//                        responseData.put("message", "Thanh toán thành công trên VNPay");
//                    }
//                }
//
//                // Redirect về trang kết quả thành công
//                return ResponseEntity.status(HttpStatus.FOUND)
//                        .header("Location", buildRedirectUrl(true, responseData))
//                        .build();
//
//            } else if (paymentStatus == 0) {
//                // Thanh toán thất bại
//                Long orderId = tryExtractOrderId(orderInfo, orderIdStr);
//
//                if (orderId != null) {
//                    try {
//                        paymentService.cancelPaymentByOrderId(orderId);
//                        log.info("Payment cancelled for order: {}", orderId);
//                    } catch (Exception e) {
//                        log.error("Error cancelling payment: {}", e.getMessage(), e);
//                    }
//                }
//
//                responseData.put("success", false);
//                responseData.put("message", "Thanh toán thất bại");
//
//                // Redirect về trang thất bại
//                return ResponseEntity.status(HttpStatus.FOUND)
//                        .header("Location", buildRedirectUrl(false, responseData))
//                        .build();
//
//            } else {
//                // Chữ ký không hợp lệ
//                responseData.put("success", false);
//                responseData.put("message", "Chữ ký không hợp lệ");
//
//                return ResponseEntity.status(HttpStatus.FOUND)
//                        .header("Location", buildRedirectUrl(false, responseData))
//                        .build();
//            }
//
//        } catch (Exception e) {
//            log.error("Error processing VNPAY callback", e);
//            return ResponseEntity.status(HttpStatus.FOUND)
//                    .header("Location", "http://localhost:4200/payment/result?success=false&message=System+error")
//                    .build();
//        }
//    }
//
//    /**
//     * Cố gắng extract orderId từ orderInfo hoặc vnp_TxnRef
//     */
//    private Long tryExtractOrderId(String orderInfo, String vnpTxnRef) {
//        try {
//            // Thử từ orderInfo: "Thanh toán đơn hàng #123"
//            if (orderInfo != null && orderInfo.contains("#")) {
//                String[] parts = orderInfo.split("#");
//                if (parts.length > 1) {
//                    return Long.parseLong(parts[1].trim());
//                }
//            }
//
//            // Thử từ vnp_TxnRef: "00000123"
//            if (vnpTxnRef != null && !vnpTxnRef.isEmpty()) {
//                return Long.parseLong(vnpTxnRef.replaceFirst("^0+(?!$)", ""));
//            }
//        } catch (NumberFormatException e) {
//            log.warn("Cannot extract orderId from orderInfo: {} or vnpTxnRef: {}", orderInfo, vnpTxnRef);
//        }
//        return null;
//    }
//
//    /**
//     * Xóa giỏ hàng sau khi thanh toán thành công
//     */
//    private void clearCartAfterPayment(Long userId, Long orderId) {
//        try {
//            log.info("Clearing cart for user {} after order {} payment", userId, orderId);
//
//            // Gọi cart service để xóa cart items
//            cartClient.clearCart(userId);
//            log.info("Cart cleared successfully for user {}", userId);
//
//        } catch (Exception e) {
//            log.error("Error clearing cart for user {}: {}", userId, e.getMessage());
//        }
//    }
//
//    /**
//     * Build redirect URL
//     */
//    private String buildRedirectUrl(boolean success, Map<String, Object> data) {
//        try {
//            StringBuilder url = new StringBuilder();
//            url.append("http://localhost:4200/payment/result?");
//            url.append("success=").append(success);
//
//            for (Map.Entry<String, Object> entry : data.entrySet()) {
//                if (entry.getValue() != null) {
//                    url.append("&").append(entry.getKey()).append("=").append(entry.getValue().toString());
//                }
//            }
//
//            return url.toString();
//        } catch (Exception e) {
//            log.error("Error building redirect URL: {}", e.getMessage());
//            return "http://localhost:4200/payment/result?success=false&message=System+error";
//        }
//    }
//}