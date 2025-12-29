package com.datn.shopcart.cache;

public class CartCacheKey {

    public static String of(Long userId, String guestId) {
        if (userId != null) {
            return "USER_" + userId;
        }
        if (guestId != null && !guestId.isBlank()) {
            return "GUEST_" + guestId;
        }
        // Thêm trường hợp cả hai đều null
        return "EMPTY_CART";
    }
}