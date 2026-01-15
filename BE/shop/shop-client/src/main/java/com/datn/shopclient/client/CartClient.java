package com.datn.shopclient.client;

import com.datn.shopobject.dto.response.CartResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "cart-service",
        url = "${cart.service.url}",
        fallbackFactory = CartClientFallbackFactory.class
)
public interface CartClient {

    @GetMapping("/api/user/cart")
    CartResponse getCart(
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "guestId", required = false) String guestId
    );

    @PostMapping("/api/user/cart/add")
    CartResponse addItem(
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "guestId", required = false) String guestId,
            @RequestParam("variantId") Long variantId,
            @RequestParam("quantity") int quantity
    );

    @PutMapping("/api/user/cart/update")
    CartResponse updateItem(
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "guestId", required = false) String guestId,
            @RequestParam("variantId") Long variantId,
            @RequestParam("quantity") int quantity
    );

    @DeleteMapping("/api/user/cart/remove")
    CartResponse removeItem(
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "guestId", required = false) String guestId,
            @RequestParam("variantId") Long variantId
    );

    @PostMapping("/api/user/cart/merge")
    CartResponse mergeGuestCart(
            @RequestParam("guestId") String guestId,
            @RequestParam("userId") Long userId
    );

    @DeleteMapping("/api/user/cart/clear")
    CartResponse clearCart(
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "guestId", required = false) String guestId
    );

    @DeleteMapping("/api/user/cart/clear/user/{userId}")
    void clearUserCart(@PathVariable("userId") Long userId);

    @DeleteMapping("/api/user/cart/clear/guest/{guestId}")
    void clearGuestCart(@PathVariable("guestId") String guestId);




}
