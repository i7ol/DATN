package com.datn.shopapp.controller;

import com.datn.shopcart.service.CartService;
import com.datn.shopobject.dto.response.CartResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/cart")
@RequiredArgsConstructor
public class CartUserController {

    private final CartService cartService;

    /* ====================== GET CART ====================== */

    @GetMapping
    public CartResponse getCart(
            @RequestParam(name = "userId",required = false) Long userId,
            @RequestParam(name = "guestId",required = false) String guestId
    ) {
        return cartService.getCartResponse(userId, guestId);
    }

    /* ====================== ADD ITEM ====================== */

    @PostMapping("/add")
    public CartResponse addItem(
            @RequestParam(name = "userId",required = false) Long userId,
            @RequestParam(name = "guestId",required = false) String guestId,
            @RequestParam("productId") Long productId,
            @RequestParam("quantity") int quantity
    ) {
        return cartService.addItem(userId, guestId, productId, quantity);
    }

    /* ====================== UPDATE ITEM ====================== */

    @PutMapping("/update")
    public CartResponse updateItem(
            @RequestParam(name = "userId",required = false) Long userId,
            @RequestParam(name = "guestId",required = false) String guestId,
            @RequestParam("productId") Long productId,
            @RequestParam("quantity") int quantity
    ) {
        return cartService.updateItem(userId, guestId, productId, quantity);
    }

    /* ====================== REMOVE ITEM ====================== */

    @DeleteMapping("/remove")
    public CartResponse removeItem(
            @RequestParam(name = "userId",required = false) Long userId,
            @RequestParam(name = "guestId",required = false) String guestId,
            @RequestParam("productId") Long productId
    ) {
        return cartService.removeItem(userId, guestId, productId);
    }

    /* ====================== MERGE CART ====================== */

    @PostMapping("/merge")
    public CartResponse mergeGuestCart(
            @RequestParam(name = "guestId",required = false) String guestId,
            @RequestParam(name = "userId",required = false) Long userId
    ) {
        cartService.mergeGuestToUser(guestId, userId);
        return cartService.getCartResponse(userId, null);
    }
}
