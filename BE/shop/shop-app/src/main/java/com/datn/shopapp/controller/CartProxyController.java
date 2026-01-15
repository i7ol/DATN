package com.datn.shopapp.controller;


import com.datn.shopclient.client.CartClient;
import com.datn.shopobject.dto.response.CartResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart-proxy")
@RequiredArgsConstructor
public class CartProxyController {

    private final CartClient cartClient;

    @GetMapping
    public CartResponse getCart(
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "guestId", required = false) String guestId
    ) {
        return cartClient.getCart(userId, guestId);
    }

    @PostMapping("/add")
    public CartResponse addItem(
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "guestId", required = false) String guestId,
            @RequestParam("variantId") Long variantId,
            @RequestParam("quantity") int quantity
    ) {
        return cartClient.addItem(userId, guestId, variantId, quantity);
    }

    @PutMapping("/update")
    public CartResponse updateItem(
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "guestId", required = false) String guestId,
            @RequestParam("variantId") Long variantId,
            @RequestParam("quantity") int quantity
    ) {
        return cartClient.updateItem(userId, guestId, variantId, quantity);
    }

    @DeleteMapping("/remove")
    public CartResponse removeItem(
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "guestId", required = false) String guestId,
            @RequestParam("variantId") Long variantId
    ) {
        return cartClient.removeItem(userId, guestId, variantId);
    }

    @PostMapping("/merge")
    public CartResponse mergeGuestCart(
            @RequestParam("guestId") String guestId,
            @RequestParam("userId") Long userId
    ) {
        return cartClient.mergeGuestCart(guestId, userId);
    }

    @DeleteMapping("/clear")
    public CartResponse clearCart(
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "guestId", required = false) String guestId
    ) {
        return cartClient.clearCart(userId, guestId);
    }

    @DeleteMapping("/clear/user/{userId}")
    public void clearUserCart(@PathVariable("userId") Long userId) {
        cartClient.clearUserCart(userId);
    }

    @DeleteMapping("/clear/guest/{guestId}")
    public void clearGuestCart(@PathVariable("guestId") String guestId) {
        cartClient.clearGuestCart(guestId);
    }
}
