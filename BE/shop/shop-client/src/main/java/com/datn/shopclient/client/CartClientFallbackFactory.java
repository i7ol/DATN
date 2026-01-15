package com.datn.shopclient.client;

import com.datn.shopclient.client.CartClient;
import com.datn.shopobject.dto.response.CartResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;

@Slf4j
@Component
public class CartClientFallbackFactory implements FallbackFactory<CartClient> {

    @Override
    public CartClient create(Throwable cause) {

        log.error("Cart-service call failed", cause);

        return new CartClient() {

            @Override
            public CartResponse getCart(Long userId, String guestId) {
                return emptyCart();
            }

            @Override
            public CartResponse addItem(Long userId, String guestId, Long variantId, int quantity) {
                return emptyCart();
            }

            @Override
            public CartResponse updateItem(Long userId, String guestId, Long variantId, int quantity) {
                return emptyCart();
            }

            @Override
            public CartResponse removeItem(Long userId, String guestId, Long variantId) {
                return emptyCart();
            }

            @Override
            public CartResponse mergeGuestCart(String guestId, Long userId) {
                return emptyCart();
            }

            @Override
            public CartResponse clearCart(Long userId, String guestId) {
                return emptyCart();
            }

            @Override
            public void clearUserCart(Long userId) {
                log.warn("clearUserCart fallback, userId={}", userId);
            }

            @Override
            public void clearGuestCart(String guestId) {
                log.warn("clearGuestCart fallback, guestId={}", guestId);
            }

            private CartResponse emptyCart() {
                return CartResponse.builder()
                        .items(Collections.emptyList())
                        .totalPrice(BigDecimal.ZERO)
                        .build();
            }
        };
    }
}
