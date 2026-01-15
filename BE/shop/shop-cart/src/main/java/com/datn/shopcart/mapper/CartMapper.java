package com.datn.shopcart.mapper;

import com.datn.shopdatabase.entity.*;
import com.datn.shopobject.dto.ImageDTO;
import com.datn.shopobject.dto.response.CartItemResponse;
import com.datn.shopobject.dto.response.CartResponse;
import com.datn.shopobject.dto.response.UserResponse;

import java.math.BigDecimal;
import java.util.List;

public class CartMapper {

    public static CartResponse toResponse(
            CartEntity cart,
            List<InventoryItemEntity> inventories
    ) {
        CartResponse res = CartResponse.builder()
                .cartId(cart.getId())
                .user(mapUser(cart.getUser()))
                .guestId(cart.getGuestId())
                .items(mapItems(cart.getItems(), inventories))
                .build();

        res.setTotalPrice(
                res.getItems().stream()
                        .map(i -> i.getPrice()
                                .multiply(BigDecimal.valueOf(i.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
        );

        res.setQuantity(
                res.getItems().stream()
                        .mapToInt(CartItemResponse::getQuantity)
                        .sum()
        );

        return res;
    }

    private static List<CartItemResponse> mapItems(
            List<CartItemEntity> items,
            List<InventoryItemEntity> inventories
    ) {
        return items.stream().map(item -> {
            ProductVariantEntity v = item.getVariant();
            ProductEntity p = v.getProduct();

            InventoryItemEntity inv = inventories.stream()
                    .filter(i -> i.getVariantId().equals(v.getId()))
                    .findFirst()
                    .orElse(null);

            return CartItemResponse.builder()
                    .variantId(v.getId())
                    .productId(p.getId())
                    .productName(p.getName())
                    .size(v.getSizeName())
                    .color(v.getColor())
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .price(item.getUnitPrice())
                    .images(mapImages(p.getImages()))
                    .build();
        }).toList();
    }

    private static List<ImageDTO> mapImages(List<ProductImageEntity> images) {
        if (images == null) return List.of();
        return images.stream()
                .map(img -> new ImageDTO(img.getId(), img.getUrl()))
                .toList();
    }

    private static UserResponse mapUser(UserEntity user) {
        if (user == null) return null;
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }
}
