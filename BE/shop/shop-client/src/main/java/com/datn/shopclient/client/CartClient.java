package com.datn.shopclient.client;

import com.datn.shopobject.dto.response.CartResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "cart-service",
        url = "${cart.service.url}",
        fallbackFactory = CartClientFallbackFactory.class
)
public interface CartClient {

    @GetMapping(value = "/api/internal/cart",produces = MediaType.APPLICATION_JSON_VALUE)
    CartResponse getCart(
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "guestId", required = false) String guestId
    );

    @PostMapping(value = "/api/internal/cart/add",produces = MediaType.APPLICATION_JSON_VALUE)
    CartResponse addItem(
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "guestId", required = false) String guestId,
            @RequestParam("variantId") Long variantId,
            @RequestParam("quantity") int quantity
    );

    @PutMapping(value = "/api/internal/cart/update",produces = MediaType.APPLICATION_JSON_VALUE)
    CartResponse updateItem(
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "guestId", required = false) String guestId,
            @RequestParam("variantId") Long variantId,
            @RequestParam("quantity") int quantity
    );

    @DeleteMapping(value = "/api/internal/cart/remove",produces = MediaType.APPLICATION_JSON_VALUE)
    CartResponse removeItem(
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "guestId", required = false) String guestId,
            @RequestParam("variantId") Long variantId
    );

    @PostMapping(value = "/api/internal/cart/merge",produces = MediaType.APPLICATION_JSON_VALUE)
    CartResponse mergeGuestCart(
            @RequestParam("guestId") String guestId,
            @RequestParam("userId") Long userId
    );

    @DeleteMapping(value = "/api/internal/cart/clear",produces = MediaType.APPLICATION_JSON_VALUE)
    CartResponse clearCart(
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "guestId", required = false) String guestId
    );

    @DeleteMapping(value = "/api/internal/cart/clear/user/{userId}",produces = MediaType.APPLICATION_JSON_VALUE)
    void clearUserCart(@PathVariable("userId") Long userId);

    @DeleteMapping(value = "/api/internal/cart/clear/guest/{guestId}",produces = MediaType.APPLICATION_JSON_VALUE)
    void clearGuestCart(@PathVariable("guestId") String guestId);




}
