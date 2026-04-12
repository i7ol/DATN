package com.datn.shopapp.controller;

import com.datn.shopapp.client.PaymentClient;
import com.datn.shopdatabase.exception.AppException;
import com.datn.shopdatabase.exception.ErrorCode;
import com.datn.shopobject.dto.request.GuestPaymentRequest;
import com.datn.shopobject.dto.request.UserPaymentRequest;
import com.datn.shopobject.dto.response.PaymentResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentProxyController {

    private final PaymentClient paymentClient;

    @PostMapping(value = "/user",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaymentResponse> userPayment(@Valid
            @RequestBody UserPaymentRequest request
    ) {
        return ResponseEntity.ok(
                paymentClient.createUserPayment(request)
        );
    }

    @PostMapping(value = "/guest",produces = MediaType.APPLICATION_JSON_VALUE)
    public PaymentResponse guestPayment(@RequestBody @Valid GuestPaymentRequest req) {

        if (req.getOrderId() == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "orderId is required");
        }

        return paymentClient.createGuestPayment(req);
    }

}

