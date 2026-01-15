package com.datn.shopapp.controller;

import com.datn.shopapp.client.PaymentClient;
import com.datn.shopapp.config.UserPrincipal;
import com.datn.shopclient.client.OrderUserClient;
import com.datn.shopdatabase.entity.OrderEntity;
import com.datn.shopdatabase.exception.AppException;
import com.datn.shopdatabase.exception.ErrorCode;
import com.datn.shopobject.dto.request.CheckoutRequest;
import com.datn.shopobject.dto.request.GuestPaymentRequest;
import com.datn.shopobject.dto.request.PaymentRequest;
import com.datn.shopobject.dto.request.UserPaymentRequest;
import com.datn.shopobject.dto.response.OrderResponse;

import com.datn.shopobject.dto.response.PaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderProxyController {

    private final OrderUserClient orderUserClient;
    private final PaymentClient paymentClient;

    @PostMapping("/checkout")
    public OrderResponse checkout(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody CheckoutRequest request
    ) {
        if (principal != null) {
            request.setUserId(principal.getId());
        }

        return orderUserClient.checkout(request);
    }


    @GetMapping("/{orderId}")
    public OrderResponse getOrder(@PathVariable Long orderId) {
        return orderUserClient.getOrder(orderId);
    }
}
