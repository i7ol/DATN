package com.datn.shopapp.client;

import com.datn.shopclient.config.FeignClientUserConfig;
import com.datn.shopobject.dto.request.GuestPaymentRequest;
import com.datn.shopobject.dto.request.UserPaymentRequest;
import com.datn.shopobject.dto.response.PaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(
        name = "payment-service",
        url = "${payment.service.url}",
        configuration = FeignClientUserConfig.class,
        fallbackFactory = PaymentClientFallbackFactory.class
)
public interface PaymentClient {

    @PostMapping("/api/payments/user")
    PaymentResponse createUserPayment(
            @RequestBody UserPaymentRequest request
    );

    @PostMapping("/api/payments/guest")
    PaymentResponse createGuestPayment(
            @RequestBody GuestPaymentRequest request
    );
}

