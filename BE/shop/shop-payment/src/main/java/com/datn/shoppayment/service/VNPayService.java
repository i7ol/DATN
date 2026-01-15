package com.datn.shoppayment.service;

import com.datn.shopobject.dto.response.VNPayPaymentResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class VNPayService {

    @Value("${vnpay.tmn-code}")
    private String vnp_TmnCode;

    @Value("${vnpay.hash-secret}")
    private String vnp_HashSecret;

    @Value("${vnpay.version:2.1.0}")
    private String vnp_Version;

    @Value("${vnpay.command:pay}")
    private String vnp_Command;

    @Value("${vnpay.order-type:other}")
    private String orderType;

    @Value("${vnpay.locale:vn}")
    private String locale;

    @Value("${vnpay.return-url}")
    private String vnp_ReturnUrl;

    @Value("${vnpay.payment-url}")
    private String vnp_PayUrl;

    @Value("${vnpay.qr-code-url}")
    private String vnp_QrCodeUrl;


    public VNPayPaymentResponse createPayment(
            Long orderId,
            Long amount,
            String orderInfo,
            String ipAddress
    ) {

        Map<String, String> vnp_Params = new HashMap<>();
        String txnRef = String.valueOf(orderId);


        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount * 100));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", txnRef);
        vnp_Params.put("vnp_OrderInfo", orderInfo);
        vnp_Params.put("vnp_OrderType", orderType);

        vnp_Params.put("vnp_Locale", locale);
        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", ipAddress);
        vnp_Params.put("vnp_CreateDate", getCurrentDate());
        vnp_Params.put("vnp_ExpireDate", getExpireDate(15));

        String paymentUrl = buildPaymentUrl(vnp_Params);

        return VNPayPaymentResponse.builder()
                .paymentUrl(paymentUrl)
                .transactionId(txnRef)
                .orderId(orderId)
                .amount(amount)
                .build();
    }


    /**
     * Xử lý kết quả trả về từ VNPay
     */
    public int orderReturn(HttpServletRequest request) {
        try {
            Map<String, String> fields = new HashMap<>();

            Enumeration<String> params = request.getParameterNames();
            while (params.hasMoreElements()) {
                String fieldName = params.nextElement();
                String fieldValue = request.getParameter(fieldName);

                if (fieldValue != null && !fieldValue.isEmpty()) {
                    fields.put(
                            URLEncoder.encode(fieldName, StandardCharsets.US_ASCII),
                            URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII)
                    );
                }
            }


            // Lấy secure hash gửi từ VNPay
            String vnpSecureHash = fields.get("vnp_SecureHash");

            // Remove hash fields trước khi ký
            fields.remove("vnp_SecureHash");
            fields.remove("vnp_SecureHashType");

            // Tạo lại chữ ký
            String signValue = hashAllFields(fields);

            //Verify chữ ký
            if (!signValue.equalsIgnoreCase(vnpSecureHash)) {
                log.error("VNPay INVALID SIGNATURE");
                return 0;
            }

            // Check kết quả giao dịch (CHUẨN)
            String responseCode = request.getParameter("vnp_ResponseCode");
            if ("00".equals(responseCode)) {
                log.info("VNPay PAYMENT SUCCESS");
                return 1;
            }

            log.warn("VNPay PAYMENT FAILED, responseCode={}", responseCode);
            return 0;

        } catch (Exception e) {
            log.error("VNPay return error", e);
            return 0;
        }
    }


    /**
     * Hash tất cả các fields
     */
    private String hashAllFields(Map<String, String> fields) {
        try {
            List<String> fieldNames = new ArrayList<>(fields.keySet());
            Collections.sort(fieldNames);

            StringBuilder sb = new StringBuilder();
            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = fields.get(fieldName);

                if (fieldValue != null && fieldValue.length() > 0) {
                    sb.append(fieldName);
                    sb.append("=");
                    sb.append(fieldValue);

                    if (itr.hasNext()) {
                        sb.append("&");
                    }
                }
            }

            return hmacSHA512(vnp_HashSecret, sb.toString());
        } catch (Exception e) {
            log.error("Error hashing fields: {}", e.getMessage(), e);
            return "";
        }
    }

    /**
     * Build payment URL
     */
    private String buildPaymentUrl(Map<String, String> params) {
        try {
            List<String> fieldNames = new ArrayList<>(params.keySet());
            Collections.sort(fieldNames);

            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();

            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = params.get(fieldName);

                if (fieldValue != null && !fieldValue.isEmpty()) {

                    hashData.append(fieldName)
                            .append("=")
                            .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()))
                            .append("=")
                            .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                    if (itr.hasNext()) {
                        hashData.append("&");
                        query.append("&");
                    }
                }
            }

            String secureHash = hmacSHA512(vnp_HashSecret, hashData.toString());
            query.append("&vnp_SecureHash=").append(secureHash);

            log.info("VNPAY HASH DATA = {}", hashData);
            log.info("VNPAY SECURE HASH = {}", secureHash);

            return vnp_PayUrl + "?" + query;

        } catch (Exception e) {
            throw new RuntimeException("Error building VNPAY URL", e);
        }
    }

    /**
     * Get current date in VNPay format
     */
    private String getCurrentDate() {
        try {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            formatter.setTimeZone(cal.getTimeZone());
            return formatter.format(cal.getTime());
        } catch (Exception e) {
            log.error("Error getting current date: {}", e.getMessage(), e);
            throw new RuntimeException("Error getting current date: " + e.getMessage());
        }
    }

    /**
     * HMAC SHA512 encryption
     */
    private String hmacSHA512(String key, String data) {
        try {
            Mac sha512_HMAC = Mac.getInstance("HmacSHA512");
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            sha512_HMAC.init(secret_key);

            byte[] hash = sha512_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString().toUpperCase();
        } catch (Exception e) {
            log.error("Error in HMAC SHA512: {}", e.getMessage(), e);
            throw new RuntimeException("Error in HMAC SHA512: " + e.getMessage());
        }
    }


    private String getExpireDate(int minutes) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        cal.add(Calendar.MINUTE, minutes);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(cal.getTimeZone());
        return formatter.format(cal.getTime());
    }

}