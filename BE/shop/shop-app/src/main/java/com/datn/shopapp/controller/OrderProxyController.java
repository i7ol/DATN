package com.datn.shopapp.controller;


import com.datn.shopobject.security.UserPrincipal;
import com.datn.shopclient.client.OrderUserClient;

import com.datn.shopobject.dto.request.CheckoutRequest;
import com.datn.shopobject.dto.response.OrderResponse;
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


    @PostMapping("/checkout")
    public OrderResponse checkout(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody CheckoutRequest request
    ) {
        request.setUserId(principal != null ? principal.getId() : null);

        return orderUserClient.checkout(request);
    }



    @GetMapping("/{orderId:\\d+}")
    public OrderResponse getOrder(@PathVariable Long orderId) {
        return orderUserClient.getOrder(orderId);
    }
}
