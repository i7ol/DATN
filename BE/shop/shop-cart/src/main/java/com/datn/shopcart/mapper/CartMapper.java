package com.datn.shopcart.mapper;

import com.datn.shopdatabase.entity.*;
import com.datn.shopobject.dto.response.*;

import java.util.List;
import com.datn.shopobject.dto.ImageDTO;

public class CartMapper {

    public static CartResponse toResponse(CartEntity cart) {
        return CartResponse.builder()
                .cartId(cart.getId())
                .user(mapUser(cart.getUser()))
                .guestId(cart.getGuestId())
                .items(mapItems(cart.getItems()))
                .build();
    }

    private static List<CartItemResponse> mapItems(List<CartItemEntity> items) {
        return items.stream()
                .map(i -> {
                    ProductEntity product = i.getProduct();

                    return CartItemResponse.builder()
                            .productId(product.getId())
                            .productName(product.getName())
                            .quantity(i.getQuantity())

                            .price(product.getPrice())
                            .images(mapImages(product.getImages()))
                            .build();
                })
                .toList();
    }

    private static List<ImageDTO> mapImages(List<ProductImageEntity> images) {
        if (images == null) return List.of();

        return images.stream()
                .map(img -> new ImageDTO(
                        img.getId(),
                        img.getUrl()
                ))
                .toList();
    }

    private static UserResponse mapUser(UserEntity user) {
        if (user == null) return null;

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .pushToken(user.getPushToken())
                .roles(
                        user.getRoles() == null
                                ? List.of()
                                : user.getRoles().stream()
                                .map(r -> r.getName())
                                .toList()
                )
                .build();
    }
}

