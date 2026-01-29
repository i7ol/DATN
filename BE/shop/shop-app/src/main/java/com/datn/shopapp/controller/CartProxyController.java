package com.datn.shopapp.controller;


import com.datn.shopapp.config.UserPrincipal;
import com.datn.shopclient.client.CartClient;
import com.datn.shopobject.dto.response.CartResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart-proxy")
@RequiredArgsConstructor
public class CartProxyController {

    private final CartClient cartClient;

    @GetMapping
    public CartResponse getCart(
            @RequestHeader(value = "X-Guest-Id", required = false) String guestId
    ) {
        Long userId = null;

        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal p) {
            userId = p.getId();
        }

        return cartClient.getCart(userId, guestId);
    }


    @PostMapping("/add")
    public CartResponse addItem(
            @RequestHeader(value = "X-Guest-Id", required = false) String guestId,
            @RequestParam("variantId") Long variantId,
            @RequestParam("quantity") int quantity
    ) {
        Long userId = getUserIdOrNull();
        return cartClient.addItem(userId, guestId, variantId, quantity);
    }


    @PutMapping("/update")
    public CartResponse updateItem(
            @RequestHeader(value = "X-Guest-Id", required = false) String guestId,
            @RequestParam("variantId") Long variantId,
            @RequestParam("quantity") int quantity
    ) {
        Long userId = getUserIdOrNull();
        return cartClient.updateItem(userId, guestId, variantId, quantity);
    }

    @DeleteMapping("/remove")
    public CartResponse removeItem(
            @RequestHeader(value = "X-Guest-Id", required = false) String guestId,
            @RequestParam("variantId") Long variantId
    ) {
        Long userId = getUserIdOrNull();
        return cartClient.removeItem(userId, guestId, variantId);
    }


    @PostMapping("/merge")
    public CartResponse mergeGuestCart(
            @RequestHeader("X-Guest-Id") String guestId
    ) {
        Long userId = getUserIdOrNull();
        if (userId == null) {
            throw new RuntimeException("User not authenticated");
        }
        return cartClient.mergeGuestCart(guestId, userId);
    }


    @DeleteMapping("/clear")
    public CartResponse clearCart(
            @RequestHeader(value = "X-Guest-Id", required = false) String guestId
    ) {
        Long userId = getUserIdOrNull();
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

    private Long getUserIdOrNull() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal p) {
            return p.getId();
        }
        return null;
    }

}
