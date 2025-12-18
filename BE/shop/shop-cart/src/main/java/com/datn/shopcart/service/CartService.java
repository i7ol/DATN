package com.datn.shopcart.service;

import com.datn.shopcart.mapper.CartMapper;
import com.datn.shopdatabase.entity.*;
import com.datn.shopdatabase.repository.*;
import com.datn.shopobject.dto.response.CartResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;


@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    /* ====================== CORE ====================== */

    public CartEntity getCart(Long userId, String guestId) {

        if (userId != null) {
            return cartRepository.findByUserId(userId)
                    .orElseGet(() -> {
                        CartEntity cart = new CartEntity();
                        cart.setUser(
                                userRepository.findById(userId)
                                        .orElseThrow(() -> new RuntimeException("User not found"))
                        );
                        return cartRepository.save(cart);
                    });
        }

        if (guestId != null && !guestId.isBlank()) {
            return cartRepository.findByGuestId(guestId)
                    .orElseGet(() -> {
                        CartEntity cart = new CartEntity();
                        cart.setGuestId(guestId);
                        return cartRepository.save(cart);
                    });
        }

        throw new IllegalArgumentException("UserId or GuestId is required");
    }

    /* ====================== CACHE READ ====================== */

    @Transactional(readOnly = true)
    @Cacheable(
            value = "cartCache",
            key = "T(com.datn.shopcart.cache.CartCacheKey).of(#userId,#guestId)",
            unless = "#result == null"
    )
    public CartResponse getCartResponse(Long userId, String guestId) {
        CartEntity cart = getCart(userId, guestId);
        CartResponse response = CartMapper.toResponse(cart);
        response.setTotalPrice(totalPrice(cart));
        return response;
    }

    /* ====================== WRITE ====================== */

    @CacheEvict(
            value = "cartCache",
            key = "T(com.datn.shopcart.cache.CartCacheKey).of(#userId,#guestId)"
    )
    public CartResponse addItem(Long userId, String guestId, Long productId, int quantity) {

        CartEntity cart = getCart(userId, guestId);
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst()
                .ifPresentOrElse(
                        i -> i.setQuantity(i.getQuantity() + quantity),
                        () -> {
                            CartItemEntity item = new CartItemEntity();
                            item.setCart(cart);
                            item.setProduct(product);
                            item.setQuantity(quantity);
                            cart.getItems().add(item);
                        }
                );

        cartRepository.save(cart);

        // ✅ GỌI TRỰC TIẾP MAPPER (KHÔNG QUA CACHE)
        CartResponse response = CartMapper.toResponse(cart);
        response.setTotalPrice(totalPrice(cart));
        return response;
    }


    @CacheEvict(
            value = "cartCache",
            key = "T(com.datn.shopcart.cache.CartCacheKey).of(#userId,#guestId)"
    )
    public CartResponse updateItem(Long userId, String guestId, Long productId, int quantity) {

        CartEntity cart = getCart(userId, guestId);

        CartItemEntity item = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Product not in cart"));

        item.setQuantity(quantity);
        cartRepository.save(cart);

        CartResponse response = CartMapper.toResponse(cart);
        response.setTotalPrice(totalPrice(cart));
        return response;
    }

    @CacheEvict(
            value = "cartCache",
            key = "T(com.datn.shopcart.cache.CartCacheKey).of(#userId,#guestId)"
    )
    public CartResponse removeItem(Long userId, String guestId, Long productId) {

        CartEntity cart = getCart(userId, guestId);
        cart.getItems().removeIf(i -> i.getProduct().getId().equals(productId));
        cartRepository.save(cart);

        CartResponse response = CartMapper.toResponse(cart);
        response.setTotalPrice(totalPrice(cart));
        return response;
    }


    /* ====================== MERGE ====================== */

    @CacheEvict(
            value = "cartCache",
            key = "T(com.datn.shopcart.cache.CartCacheKey).of(#userId,null)"
    )
    public void mergeGuestToUser(String guestId, Long userId) {

        CartEntity guestCart = cartRepository.findByGuestId(guestId).orElse(null);
        if (guestCart == null) return;

        CartEntity userCart = getCart(userId, null);

        for (CartItemEntity item : guestCart.getItems()) {
            userCart.getItems().add(item);
            item.setCart(userCart);
        }

        cartRepository.delete(guestCart);
        cartRepository.save(userCart);
    }


    /* ====================== PRICE ====================== */

    private BigDecimal totalPrice(CartEntity cart) {
        return cart.getItems().stream()
                .map(i -> i.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

