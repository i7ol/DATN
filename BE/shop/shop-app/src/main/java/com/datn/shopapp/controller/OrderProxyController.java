package com.datn.shopapp.controller;


import com.datn.shopapp.config.UserPrincipal;
import com.datn.shopclient.client.OrderUserClient;
import com.datn.shopobject.dto.request.CheckoutRequest;
import com.datn.shopobject.dto.response.OrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        if (principal != null) {
            request.setUserId(principal.getId());
        }

        return orderUserClient.checkout(request);
    }
    @GetMapping("/my")
    public Page<OrderResponse> myOrders(
            @AuthenticationPrincipal UserPrincipal principal,
            Pageable pageable
    ) {
        if (principal == null) {
            throw new RuntimeException("Unauthorized");
        }

        return orderUserClient.myOrders(principal.getId(), pageable);
    }




    @GetMapping("/{orderId}")
    public OrderResponse getOrder(@PathVariable Long orderId) {
        return orderUserClient.getOrder(orderId);
    }
}
