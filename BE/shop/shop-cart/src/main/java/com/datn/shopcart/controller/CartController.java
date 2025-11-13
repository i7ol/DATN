package com.datn.shopcart.controller;

import com.datn.shopcart.dto.request.AddCartItemRequest;
import com.datn.shopcart.dto.request.UpdateCartItemRequest;
import com.datn.shopcart.dto.response.CartResponse;
import com.datn.shopcart.dto.response.CartItemResponse;
import com.datn.shopcart.dto.response.UserResponse;
import com.datn.shopcart.entity.Cart;
import com.datn.shopcart.service.CartService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/carts")
@AllArgsConstructor
public class CartController {

    CartService cartService;

    private CartResponse mapToResponse(Cart cart) {
        UserResponse userResponse = null;
        if (cart.getUser() != null) {
            userResponse = new UserResponse(
                    cart.getUser().getId(),
                    cart.getUser().getUsername(),
                    cart.getUser().getEmail(),
                    cart.getUser().getPhone(),
                    cart.getUser().getAddress()
            );
        }

        List<CartItemResponse> items = cart.getItems().stream().map(item ->
                new CartItemResponse(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                )
        ).toList();

        return new CartResponse(cart.getId(), userResponse, cart.getGuestId(), items);
    }

    @GetMapping
    public CartResponse getCart(@RequestParam(name = "userId", required = false) Long userId,
                           @RequestParam(name = "guestId", required = false) String guestId) {
        Cart cart = cartService.getCart(userId, guestId);
        return mapToResponse(cart);
    }

    @PostMapping("/add")
    public CartResponse addItem(@RequestParam(name = "userId", required = false) Long userId,
                                @RequestParam(name = "guestId", required = false) String guestId,
                           @RequestBody @Valid AddCartItemRequest request) {
        Cart cart = cartService.addItem(userId, guestId, request.getProductId(), request.getQuantity());
        return mapToResponse(cart);
    }

    @PutMapping("/update/{productId}")
    public CartResponse updateItem(@RequestParam(name = "userId",required = false) Long userId,
                              @RequestParam(name = "guestId",required = false) String guestId,
                              @PathVariable("productId") Long productId,
                              @RequestBody @Valid UpdateCartItemRequest request) {
        Cart cart = cartService.updateItemQuantity(userId, guestId, productId, request.getQuantity());
        return mapToResponse(cart);
    }

    @DeleteMapping("/remove")
    public CartResponse removeItem(@RequestParam(name = "userId",required = false) Long userId,
                              @RequestParam(name = "guestId",required = false) String guestId,
                              @RequestParam(name = "productId") Long productId) {
        Cart cart = cartService.removeItem(userId, guestId, productId);
        return mapToResponse(cart);
    }

    @PostMapping("/merge")
    public void mergeGuestCart(@RequestParam(name = "guestId") String guestId,
                               @RequestParam(name = "userId") Long userId) {
        cartService.mergeGuestCartToUser(guestId, userId);
    }

    @GetMapping("/total")
    public BigDecimal getTotal(@RequestParam(name = "userId",required = false) Long userId,
                               @RequestParam(name = "guestId",required = false) String guestId) {
        return cartService.getTotalPrice(userId, guestId);
    }
}
