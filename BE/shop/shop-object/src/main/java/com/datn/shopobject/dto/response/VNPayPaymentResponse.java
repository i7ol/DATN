// VNPayPaymentResponse.java
package com.datn.shopobject.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VNPayPaymentResponse {
    private String paymentUrl;      // URL thanh toán đầy đủ
    private String qrCodeUrl;       // URL QR code
    private Long orderId;
    private Long amount;
    private String transactionId;
}