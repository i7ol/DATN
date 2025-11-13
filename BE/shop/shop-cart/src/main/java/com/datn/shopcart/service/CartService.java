package com.datn.shopcart.service;

import com.datn.shopcart.entity.Cart;
import com.datn.shopcart.entity.CartItem;
import com.datn.shopcart.repository.CartItemRepository;
import com.datn.shopcart.repository.CartRepository;
import com.datn.shopproduct.entity.Product;
import com.datn.shopproduct.repository.ProductRepository;
import com.datn.shopcore.entity.User;
import com.datn.shopcore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartService {

    CartRepository cartRepository;
    CartItemRepository cartItemRepository;
    ProductRepository productRepository;
    UserRepository userRepository;

    @Transactional
    public Cart getCart(Long userId, String guestId) {
        if (userId != null) {
            return cartRepository.findByUserId(userId)
                    .orElseGet(() -> {
                        User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("User not found"));
                        Cart cart = new Cart();
                        cart.setUser(user);
                        cart.setItems(new ArrayList<>());
                        return cartRepository.save(cart);
                    });
        } else if (guestId != null) {
            return cartRepository.findByGuestId(guestId)
                    .orElseGet(() -> {
                        Cart cart = new Cart();
                        cart.setGuestId(guestId);
                        cart.setItems(new ArrayList<>());
                        return cartRepository.save(cart);
                    });
        } else {
            throw new RuntimeException("userId hoặc guestId phải khác null");
        }
    }

    @Transactional
    public Cart addItem(Long userId, String guestId, Long productId, int quantity) {
        // Lấy cart của user hoặc guest
        Cart cart = getCart(userId, guestId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Check trực tiếp trong DB xem đã có CartItem này chưa
        CartItem existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            cartItemRepository.save(existingItem);
        } else {
            CartItem newItem = new CartItem();
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            newItem.setCart(cart);
            cart.getItems().add(newItem);
        }

        // Tải lại cart đầy đủ để trả về
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart updateItemQuantity(Long userId, String guestId, Long productId, int quantity) {
        Cart cart = getCart(userId, guestId);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Product not in cart"));

        item.setQuantity(quantity);

        return cartRepository.save(cart);
    }

    @Transactional
    public Cart removeItem(Long userId, String guestId, Long productId) {
        Cart cart = getCart(userId, guestId);
        cart.getItems().removeIf(i -> i.getProduct().getId().equals(productId));
        return cartRepository.save(cart);
    }

    @Transactional
    public void mergeGuestCartToUser(String guestId, Long userId) {
        Cart guestCart = cartRepository.findByGuestId(guestId).orElse(null);
        if (guestCart == null) return;

        Cart userCart = getCart(userId, null);

        for (CartItem item : guestCart.getItems()) {
            CartItem existingItem = userCart.getItems().stream()
                    .filter(i -> i.getProduct().getId().equals(item.getProduct().getId()))
                    .findFirst()
                    .orElse(null);

            if (existingItem != null) {
                existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
            } else {
                item.setCart(userCart);
                userCart.getItems().add(item);
            }
        }

        cartRepository.save(userCart);
        cartRepository.delete(guestCart);
    }

    public BigDecimal getTotalPrice(Long userId, String guestId) {
        Cart cart = getCart(userId, guestId);
        return cart.getItems().stream()
                .map(i -> i.getProduct().getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
