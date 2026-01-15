package com.datn.shoppayment.service;

import com.datn.shopobject.dto.request.GuestPaymentRequest;
import com.datn.shopobject.dto.request.UserPaymentRequest;
import com.datn.shopobject.dto.response.PaymentResponse;

public interface PaymentService {

    PaymentResponse createUserPayment(
            UserPaymentRequest request,
            Long userId,
            String ipAddress
    );

    PaymentResponse createGuestPayment(
            GuestPaymentRequest request,
            String ipAddress
    );
}
