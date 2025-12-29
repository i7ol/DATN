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

    /**
     * Tạo URL thanh toán VNPay với 4 tham số
     */
    public VNPayPaymentResponse createPayment(Long orderId, Long amount, String orderInfo, String ipAddress) {
        try {
            // Format orderId thành 8 chữ số (yêu cầu của VNPAY)
            String vnp_TxnRef = String.format("%08d", orderId);

            // Default IP nếu không có
            if (ipAddress == null || ipAddress.isEmpty()) {
                ipAddress = "127.0.0.1";
            }

            Map<String, String> vnp_Params = new TreeMap<>();

            // Các tham số bắt buộc
            vnp_Params.put("vnp_Version", vnp_Version);
            vnp_Params.put("vnp_Command", vnp_Command);
            vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
            vnp_Params.put("vnp_Amount", String.valueOf(amount * 100)); // VNPAY yêu cầu x100
            vnp_Params.put("vnp_CreateDate", getCurrentDate());
            vnp_Params.put("vnp_CurrCode", "VND");
            vnp_Params.put("vnp_IpAddr", ipAddress);
            vnp_Params.put("vnp_Locale", locale);
            vnp_Params.put("vnp_OrderInfo", orderInfo);
            vnp_Params.put("vnp_OrderType", orderType);
            vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
            vnp_Params.put("vnp_TxnRef", vnp_TxnRef);

            // Tạo URL thanh toán
            String paymentUrlWithParams = buildPaymentUrl(vnp_Params);

            // Tạo QR code URL
            String qrCodeUrl = generateQRCodeUrl(vnp_Params);

            log.info("VNPAY payment created - Order: {}, Amount: {}, URL: {}",
                    orderId, amount, paymentUrlWithParams);

            return VNPayPaymentResponse.builder()
                    .paymentUrl(paymentUrlWithParams)
                    .qrCodeUrl(qrCodeUrl)
                    .orderId(orderId)
                    .amount(amount)
                    .transactionId(vnp_TxnRef)
                    .build();

        } catch (Exception e) {
            log.error("Error creating VNPay payment for order {}: {}", orderId, e.getMessage(), e);
            throw new RuntimeException("Không thể tạo thanh toán VNPAY: " + e.getMessage());
        }
    }

    /**
     * Tạo URL thanh toán với 3 tham số (giống mẫu cũ)
     */
    public String createOrder(int total, String orderInfo, String baseUrl) {
        try {
            String vnp_TxnRef = getRandomNumber(8);

            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_Version", vnp_Version);
            vnp_Params.put("vnp_Command", vnp_Command);
            vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
            vnp_Params.put("vnp_Amount", String.valueOf(total * 100));
            vnp_Params.put("vnp_CurrCode", "VND");
            vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
            vnp_Params.put("vnp_OrderInfo", orderInfo);
            vnp_Params.put("vnp_OrderType", orderType);
            vnp_Params.put("vnp_Locale", locale);

            String returnUrl = baseUrl + vnp_ReturnUrl;
            vnp_Params.put("vnp_ReturnUrl", returnUrl);

            String vnp_IpAddr = "127.0.0.1";
            vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String vnp_CreateDate = formatter.format(cld.getTime());
            vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

            cld.add(Calendar.MINUTE, 15);
            String vnp_ExpireDate = formatter.format(cld.getTime());
            vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

            // Sắp xếp các tham số theo key
            List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
            Collections.sort(fieldNames);

            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();

            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = vnp_Params.get(fieldName);

                if (fieldValue != null && fieldValue.length() > 0) {
                    hashData.append(fieldName);
                    hashData.append('=');
                    try {
                        hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                        query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                        query.append('=');
                        query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    } catch (UnsupportedEncodingException e) {
                        log.error("Encoding error: {}", e.getMessage());
                        throw new RuntimeException("Encoding error: " + e.getMessage());
                    }

                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                }
            }

            String queryUrl = query.toString();
            String vnp_SecureHash = hmacSHA512(vnp_HashSecret, hashData.toString());
            queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

            return vnp_PayUrl + "?" + queryUrl;

        } catch (Exception e) {
            log.error("Error creating VNPay order: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể tạo thanh toán VNPAY: " + e.getMessage());
        }
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
                if (fieldValue != null && fieldValue.length() > 0) {
                    fields.put(fieldName, fieldValue);
                }
            }

            String vnp_SecureHash = request.getParameter("vnp_SecureHash");

            if (fields.containsKey("vnp_SecureHashType")) {
                fields.remove("vnp_SecureHashType");
            }
            if (fields.containsKey("vnp_SecureHash")) {
                fields.remove("vnp_SecureHash");
            }

            // Tạo chữ ký từ các fields còn lại
            String signValue = hashAllFields(fields);

            // So sánh chữ ký
            if (signValue.equals(vnp_SecureHash)) {
                // Kiểm tra trạng thái giao dịch
                if ("00".equals(request.getParameter("vnp_TransactionStatus"))) {
                    return 1; // Thành công
                } else {
                    return 0; // Thất bại
                }
            } else {
                return -1; // Sai chữ ký
            }

        } catch (Exception e) {
            log.error("Error processing VNPay return: {}", e.getMessage(), e);
            return -1;
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
            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();

            Iterator<Map.Entry<String, String>> itr = params.entrySet().iterator();
            while (itr.hasNext()) {
                Map.Entry<String, String> entry = itr.next();
                String key = entry.getKey();
                String value = entry.getValue();

                if (value != null && !value.isEmpty()) {
                    // Build hash data
                    hashData.append(key).append("=").append(value);

                    // Build query
                    query.append(URLEncoder.encode(key, StandardCharsets.UTF_8.toString()))
                            .append("=")
                            .append(value);

                    if (itr.hasNext()) {
                        hashData.append("&");
                        query.append("&");
                    }
                }
            }

            // Tạo secure hash
            String vnp_SecureHash = hmacSHA512(vnp_HashSecret, hashData.toString());
            query.append("&vnp_SecureHash=").append(vnp_SecureHash);

            return vnp_PayUrl + "?" + query;
        } catch (Exception e) {
            log.error("Error building payment URL: {}", e.getMessage(), e);
            throw new RuntimeException("Error building payment URL: " + e.getMessage());
        }
    }

    /**
     * Generate QR code URL
     */
    private String generateQRCodeUrl(Map<String, String> params) {
        try {
            String paymentUrl = buildPaymentUrl(params);
            // Tạo QR code URL cho VNPAY
            return vnp_QrCodeUrl + "?data=" + URLEncoder.encode(paymentUrl, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            log.error("Error generating QR code URL: {}", e.getMessage(), e);
            throw new RuntimeException("Error generating QR code URL: " + e.getMessage());
        }
    }

    /**
     * Get current date in VNPay format
     */
    private String getCurrentDate() {
        try {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
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

    /**
     * Validate payment signature
     */
    public boolean validatePayment(Map<String, String> params) {
        try {
            String vnp_SecureHash = params.remove("vnp_SecureHash");
            if (vnp_SecureHash == null) {
                log.error("vnp_SecureHash is null");
                return false;
            }

            // Sắp xếp params theo key
            Map<String, String> sortedParams = new TreeMap<>(params);

            // Tạo hash data
            StringBuilder hashData = new StringBuilder();
            Iterator<Map.Entry<String, String>> itr = sortedParams.entrySet().iterator();
            while (itr.hasNext()) {
                Map.Entry<String, String> entry = itr.next();
                String key = entry.getKey();
                String value = entry.getValue();

                if (value != null && !value.isEmpty()) {
                    hashData.append(key).append("=").append(value);
                    if (itr.hasNext()) {
                        hashData.append("&");
                    }
                }
            }

            // Tính toán hash
            String calculatedHash = hmacSHA512(vnp_HashSecret, hashData.toString());

            // So sánh hash
            boolean isValid = calculatedHash.equalsIgnoreCase(vnp_SecureHash);
            if (!isValid) {
                log.error("Hash mismatch. Calculated: {}, Received: {}", calculatedHash, vnp_SecureHash);
            }
            return isValid;

        } catch (Exception e) {
            log.error("Error validating VNPay payment: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Generate random number
     */
    public static String getRandomNumber(int len) {
        Random rnd = new Random();
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * MD5 encryption
     */
    public static String md5(String message) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(message.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            log.error("Error in MD5: {}", ex.getMessage());
            return "";
        }
    }

    /**
     * SHA256 encryption
     */
    public static String Sha256(String message) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(message.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            log.error("Error in SHA256: {}", ex.getMessage());
            return "";
        }
    }

    /**
     * Get IP address
     */
    public static String getIpAddress(HttpServletRequest request) {
        try {
            String ipAddress = request.getHeader("X-FORWARDED-FOR");
            if (ipAddress == null) {
                ipAddress = request.getRemoteAddr();
            }
            return ipAddress;
        } catch (Exception e) {
            log.error("Error getting IP address: {}", e.getMessage());
            return "127.0.0.1";
        }
    }
}