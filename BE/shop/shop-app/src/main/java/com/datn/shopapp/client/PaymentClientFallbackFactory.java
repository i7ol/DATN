package com.datn.shopapp.client;

import com.datn.shopobject.dto.request.GuestPaymentRequest;
import com.datn.shopobject.dto.request.PaymentRequest;
import com.datn.shopobject.dto.request.UserPaymentRequest;
import com.datn.shopobject.dto.response.PaymentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PaymentClientFallbackFactory
        implements FallbackFactory<PaymentClient> {

    @Override
    public PaymentClient create(Throwable cause) {

        log.error("Payment service unavailable", cause);

        return new PaymentClient() {

            @Override
            public PaymentResponse createUserPayment(UserPaymentRequest request) {
                throw new RuntimeException("Payment service unavailable", cause);
            }

            @Override
            public PaymentResponse createGuestPayment(GuestPaymentRequest request) {
                throw new RuntimeException("Payment service unavailable", cause);
            }
        };
    }
}
