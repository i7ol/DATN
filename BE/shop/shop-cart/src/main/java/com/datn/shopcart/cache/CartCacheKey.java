package com.datn.shopcart.cache;

public class CartCacheKey {

    public static String of(Long userId, String guestId) {
        if (userId != null) {
            return "CART::USER::" + userId;
        }
        return "CART::GUEST::" + guestId;
    }
}
