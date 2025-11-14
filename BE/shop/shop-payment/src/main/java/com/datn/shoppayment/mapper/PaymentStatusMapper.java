package com.datn.shoppayment.mapper;

import com.datn.shoppayment.enums.PaymentStatus;

public class PaymentStatusMapper {

    // Map từ PaymentStatus (payment module) sang Order's payment status string expected by order service
    public static String toOrderPaymentStatus(PaymentStatus status) {
        return switch (status) {
            case PENDING -> "PENDING";
            case SUCCESS -> "PAID";   // map SUCCESS -> PAID (order enum)
            case FAILED  -> "FAILED";
        };
    }
}
