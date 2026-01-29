package com.datn.shopcart.controller;

import com.datn.shopcart.service.CartService;
import com.datn.shopobject.dto.response.CartResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/internal/cart")
@RequiredArgsConstructor
public class CartUserController {

    private final CartService cartService;

    /* ====================== GET CART ====================== */

    @GetMapping
    public CartResponse getCart(
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "guestId", required = false) String guestId
    ) {
        return cartService.getCartResponse(userId, guestId);
    }

    /* ====================== ADD ITEM ====================== */

    @PostMapping("/add")
    public CartResponse addItem(
            @RequestParam("variantId") Long variantId,
            @RequestParam("quantity") int quantity,
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "guestId", required = false) String guestId
    ) {
        return cartService.addItem(variantId, quantity, userId, guestId);
    }

    /* ====================== UPDATE ITEM ====================== */

    @PutMapping("/update")
    public CartResponse updateItem(
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "guestId", required = false) String guestId,
            @RequestParam("variantId") Long variantId,
            @RequestParam("quantity") int quantity
    ) {
        return cartService.updateItem(userId, guestId, variantId, quantity);
    }

    /* ====================== REMOVE ITEM ====================== */

    @DeleteMapping("/remove")
    public CartResponse removeItem(
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "guestId", required = false) String guestId,
            @RequestParam("variantId") Long variantId
    ) {
        return cartService.removeItem(userId, guestId, variantId);
    }

    /* ====================== MERGE CART ====================== */

    @PostMapping("/merge")
    public CartResponse mergeGuestCart(
            @RequestParam(name = "guestId") String guestId,
            @RequestParam(name = "userId") Long userId
    ) {
        cartService.mergeGuestToUser(guestId, userId);
        return cartService.getCartResponse(userId, null);
    }


    /* ====================== CLEAR CART ====================== */
    @DeleteMapping("/clear")
    public CartResponse clearCart(
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "guestId", required = false) String guestId
    ) {
        return cartService.clearCart(userId, guestId);
    }


    @DeleteMapping("/clear/user/{userId}")
    public void clearUserCart(@PathVariable("userId") Long userId) {
        cartService.clearUserCart(userId);
    }

    @DeleteMapping("/clear/guest/{guestId}")
    public void clearGuestCart(@PathVariable("guestId") String guestId) {
        cartService.clearGuestCart(guestId);
    }
}
