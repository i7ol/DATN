package com.datn.shopcart.service;


import com.datn.shopcart.mapper.CartMapper;
import com.datn.shopclient.client.InventoryClient;
import com.datn.shopdatabase.entity.*;
import com.datn.shopdatabase.repository.*;
import com.datn.shopobject.dto.response.CartResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository variantRepository;
    private final InventoryRepository inventoryRepository;
    private final UserRepository userRepository;
    private final InventoryClient inventoryClient;

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
                        return cartRepository.save(cart); // lưu ngay
                    });
        }

        if (guestId != null && !guestId.isBlank()) {
            return cartRepository.findByGuestId(guestId)
                    .orElseGet(() -> {
                        CartEntity cart = new CartEntity();
                        cart.setGuestId(guestId);
                        CartEntity savedCart = cartRepository.save(cart); // lưu trước
                        return savedCart;
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

        List<Long> variantIds = cart.getItems().stream()
                .map(CartItemEntity::getVariant)
                .filter(v -> v != null)
                .map(ProductVariantEntity::getId)
                .toList();


        List<InventoryItemEntity> inventories =
                variantIds.isEmpty()
                        ? List.of()
                        : inventoryRepository.findByVariantIdIn(variantIds);

        return CartMapper.toResponse(cart, inventories);
    }

    /* ====================== ADD ITEM ====================== */

    @CacheEvict(
            value = "cartCache",
            key = "T(com.datn.shopcart.cache.CartCacheKey).of(#userId,#guestId)"
    )
    public CartResponse addItem(
            Long variantId,
            int quantity,
            Long userId,
            String guestId
    ) {

        CartEntity cart = getCart(userId, guestId);

        ProductVariantEntity variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Variant not found"));

        CartItemEntity item = cartItemRepository
                .findByCartIdAndVariantId(cart.getId(), variantId)
                .orElse(null);


        if (item != null) {
            item.setQuantity(item.getQuantity() + quantity);
        } else {
            InventoryItemEntity inv = inventoryRepository
                    .findByVariantId(variantId)
                    .orElseThrow(() -> new RuntimeException("Inventory not found"));

            CartItemEntity newItem = new CartItemEntity();
            newItem.setCart(cart);
            newItem.setVariant(variant);
            newItem.setUnitPrice(inv.getSellingPrice());
            newItem.setQuantity(quantity);
            cart.getItems().add(newItem);
        }

        cartRepository.save(cart);
        inventoryClient.reserve(variantId, quantity);

        return getCartResponse(userId, guestId);
    }

    /* ====================== UPDATE ITEM ====================== */

    @CacheEvict(
            value = "cartCache",
            key = "T(com.datn.shopcart.cache.CartCacheKey).of(#userId,#guestId)"
    )
    public CartResponse updateItem(
            Long userId,
            String guestId,
            Long variantId,
            int quantity
    ) {

        CartEntity cart = getCart(userId, guestId);

        CartItemEntity item = cartItemRepository
                .findByCartIdAndVariantId(cart.getId(), variantId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        int oldQty = item.getQuantity();

        if (quantity < 1) {
            cart.getItems().remove(item);
            inventoryClient.release(variantId, oldQty);

        } else {
            item.setQuantity(quantity);
            int diff = quantity - oldQty;
            if (diff > 0) inventoryClient.reserve(variantId, diff);
            else if (diff < 0) inventoryClient.release(variantId, -diff);
        }

        cartRepository.save(cart);

        return getCartResponse(userId, guestId);
    }

    /* ====================== REMOVE ITEM ====================== */

    @CacheEvict(
            value = "cartCache",
            key = "T(com.datn.shopcart.cache.CartCacheKey).of(#userId,#guestId)"
    )
    public CartResponse removeItem(
            Long userId,
            String guestId,
            Long variantId
    ) {

        CartEntity cart = getCart(userId, guestId);
        CartItemEntity item = cart.getItems().stream()
                .filter(i -> i.getVariant() != null && i.getVariant().getId().equals(variantId))
                .findFirst()
                .orElse(null);

        if (item != null) {
            inventoryClient.release(variantId, item.getQuantity());
            cart.getItems().remove(item);
            cartRepository.save(cart);
        }

        cartRepository.save(cart);

        return getCartResponse(userId, guestId);
    }

    /* ====================== MERGE GUEST → USER ====================== */

    @CacheEvict(
            value = "cartCache",
            key = "T(com.datn.shopcart.cache.CartCacheKey).of(#userId,null)"
    )
    public void mergeGuestToUser(String guestId, Long userId) {

        CartEntity guestCart = cartRepository.findByGuestId(guestId).orElse(null);
        if (guestCart == null) return;

        CartEntity userCart = getCart(userId, null);

        for (CartItemEntity guestItem : guestCart.getItems()) {

            if (guestItem.getVariant() == null) continue;

            Long variantId = guestItem.getVariant().getId();

            CartItemEntity userItem = cartItemRepository
                    .findByCartIdAndVariantId(userCart.getId(), variantId)
                    .orElse(null);

            if (userItem != null) {
                userItem.setQuantity(
                        userItem.getQuantity() + guestItem.getQuantity()
                );
            } else {
                guestItem.setCart(userCart);
                userCart.getItems().add(guestItem);
            }
        }


        cartRepository.delete(guestCart);
        cartRepository.save(userCart);
    }

    /* ====================== CLEAR ====================== */

    @CacheEvict(
            value = "cartCache",
            key = "T(com.datn.shopcart.cache.CartCacheKey).of(#userId,#guestId)"
    )
    public CartResponse clearCart(Long userId, String guestId) {

        CartEntity cart = getCart(userId, guestId);
        for (CartItemEntity item : cart.getItems()) {
            if (item.getVariant() != null) {
                inventoryClient.release(item.getVariant().getId(), item.getQuantity());
            }
        }
        cart.getItems().clear();
        cartRepository.save(cart);


        return getCartResponse(userId, guestId);
    }


    public void clearUserCart(Long userId) {
        clearCart(userId, null);
    }

    public void clearGuestCart(String guestId) {
        clearCart(null, guestId);
    }

}

