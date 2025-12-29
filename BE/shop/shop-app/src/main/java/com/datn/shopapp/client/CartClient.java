package com.datn.shopapp.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "cart-service", url = "${cart.service.url}")
public interface CartClient {

    @DeleteMapping("/user/cart/clear/{userId}")
    void clearCart(@PathVariable Long userId);
}