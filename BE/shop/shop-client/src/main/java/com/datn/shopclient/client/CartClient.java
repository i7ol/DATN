package com.datn.shopclient.client;

import com.datn.shopobject.dto.response.CartResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "cart-service",
        url = "${cart.service.url}",
        fallbackFactory = CartClientFallbackFactory.class
)
public interface CartClient {

    @GetMapping("/api/internal/cart")
    CartResponse getCart(
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "guestId", required = false) String guestId
    );

    @PostMapping("/api/internal/cart/add")
    CartResponse addItem(
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "guestId", required = false) String guestId,
            @RequestParam("variantId") Long variantId,
            @RequestParam("quantity") int quantity
    );

    @PutMapping("/api/internal/cart/update")
    CartResponse updateItem(
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "guestId", required = false) String guestId,
            @RequestParam("variantId") Long variantId,
            @RequestParam("quantity") int quantity
    );

    @DeleteMapping("/api/internal/cart/remove")
    CartResponse removeItem(
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "guestId", required = false) String guestId,
            @RequestParam("variantId") Long variantId
    );

    @PostMapping("/api/internal/cart/merge")
    CartResponse mergeGuestCart(
            @RequestParam("guestId") String guestId,
            @RequestParam("userId") Long userId
    );

    @DeleteMapping("/api/internal/cart/clear")
    CartResponse clearCart(
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "guestId", required = false) String guestId
    );

    @DeleteMapping("/api/internal/cart/clear/user/{userId}")
    void clearUserCart(@PathVariable("userId") Long userId);

    @DeleteMapping("/api/internal/cart/clear/guest/{guestId}")
    void clearGuestCart(@PathVariable("guestId") String guestId);




}
