//package com.datn.shopcart.service;
//
//import com.datn.shopdatabase.entity.CartEntity;
//import com.datn.shopdatabase.entity.CartItemEntity;
//import com.datn.shopdatabase.repository.CartItemRepository;
//import com.datn.shopdatabase.repository.CartRepository;
//import com.datn.shopdatabase.entity.ProductEntity;
//import com.datn.shopdatabase.repository.ProductRepository;
//import com.datn.shopdatabase.entity.UserEntity;
//import com.datn.shopdatabase.repository.UserRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.experimental.FieldDefaults;
//import lombok.AccessLevel;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal;
//import java.util.ArrayList;
//
//@Service
//@RequiredArgsConstructor
//@FieldDefaults(level = AccessLevel.PRIVATE)
//public class CartService {
//
//    CartRepository cartRepository;
//    CartItemRepository cartItemRepository;
//    ProductRepository productRepository;
//    UserRepository userRepository;
//
//    @Transactional
//    public CartEntity getCart(Long userId, String guestId) {
//        if (userId != null) {
//            return cartRepository.findByUserId(userId)
//                    .orElseGet(() -> {
//                        UserEntity user = userRepository.findById(userId)
//                                .orElseThrow(() -> new RuntimeException("User not found"));
//                        CartEntity cart = new CartEntity();
//                        cart.setUser(user);
//                        cart.setItems(new ArrayList<>());
//                        return cartRepository.save(cart);
//                    });
//        } else if (guestId != null) {
//            return cartRepository.findByGuestId(guestId)
//                    .orElseGet(() -> {
//                        CartEntity cart = new CartEntity();
//                        cart.setGuestId(guestId);
//                        cart.setItems(new ArrayList<>());
//                        return cartRepository.save(cart);
//                    });
//        } else {
//            throw new RuntimeException("userId hoặc guestId phải khác null");
//        }
//    }
//
//    @Transactional
//    public CartEntity addItem(Long userId, String guestId, Long productId, int quantity) {
//        // Lấy cart của user hoặc guest
//        CartEntity cart = getCart(userId, guestId);
//
//        ProductEntity product = productRepository.findById(productId)
//                .orElseThrow(() -> new RuntimeException("Product not found"));
//
//        // Check trực tiếp trong DB xem đã có CartItem này chưa
//        CartItemEntity existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
//                .orElse(null);
//
//        if (existingItem != null) {
//            existingItem.setQuantity(existingItem.getQuantity() + quantity);
//            cartItemRepository.save(existingItem);
//        } else {
//            CartItemEntity newItem = new CartItemEntity();
//            newItem.setProduct(product);
//            newItem.setQuantity(quantity);
//            newItem.setCart(cart);
//            cart.getItems().add(newItem);
//        }
//
//        // Tải lại cart đầy đủ để trả về
//        return cartRepository.save(cart);
//    }
//
//    @Transactional
//    public CartEntity updateItemQuantity(Long userId, String guestId, Long productId, int quantity) {
//        CartEntity cart = getCart(userId, guestId);
//
//        CartItemEntity item = cart.getItems().stream()
//                .filter(i -> i.getProduct().getId().equals(productId))
//                .findFirst()
//                .orElseThrow(() -> new RuntimeException("Product not in cart"));
//
//        item.setQuantity(quantity);
//
//        return cartRepository.save(cart);
//    }
//
//    @Transactional
//    public CartEntity removeItem(Long userId, String guestId, Long productId) {
//        CartEntity cart = getCart(userId, guestId);
//        cart.getItems().removeIf(i -> i.getProduct().getId().equals(productId));
//        return cartRepository.save(cart);
//    }
//
//    @Transactional
//    public void mergeGuestCartToUser(String guestId, Long userId) {
//        CartEntity guestCart = cartRepository.findByGuestId(guestId).orElse(null);
//        if (guestCart == null) return;
//
//        CartEntity userCart = getCart(userId, null);
//
//        for (CartItemEntity item : guestCart.getItems()) {
//            CartItemEntity existingItem = userCart.getItems().stream()
//                    .filter(i -> i.getProduct().getId().equals(item.getProduct().getId()))
//                    .findFirst()
//                    .orElse(null);
//
//            if (existingItem != null) {
//                existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
//            } else {
//                item.setCart(userCart);
//                userCart.getItems().add(item);
//            }
//        }
//
//        cartRepository.save(userCart);
//        cartRepository.delete(guestCart);
//    }
//
//    public BigDecimal getTotalPrice(Long userId, String guestId) {
//        CartEntity cart = getCart(userId, guestId);
//        return cart.getItems().stream()
//                .map(i -> i.getProduct().getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//    }
//}
