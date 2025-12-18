// OrderUserController.java - Đặt trong shop-app
package com.datn.shopapp.controller;

import com.datn.shopapp.config.UserPrincipal;
import com.datn.shopcart.service.CartService;
import com.datn.shopdatabase.entity.CartEntity;
import com.datn.shopdatabase.entity.OrderEntity;
import com.datn.shopdatabase.entity.OrderItemEntity;
import com.datn.shopdatabase.entity.ProductEntity;
import com.datn.shopdatabase.enums.OrderStatus;
import com.datn.shopdatabase.enums.PaymentStatus;
import com.datn.shopdatabase.exception.AppException;
import com.datn.shopdatabase.exception.ErrorCode;
import com.datn.shopdatabase.repository.CartRepository;
import com.datn.shopobject.dto.request.CheckoutRequest;
import com.datn.shopobject.dto.response.OrderResponse;
import com.datn.shoporder.mapper.OrderMapper;
import com.datn.shoporder.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")  // Giữ nguyên /api/orders cho shop-app
@RequiredArgsConstructor
public class OrderUserController {

    private final OrderService orderService;
    private final CartService cartService;
    private final CartRepository cartRepository;

    // =========================
    // CHECKOUT (USER + GUEST)
    // =========================
    @PostMapping("/checkout")
    @Transactional
    public OrderResponse checkout(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody CheckoutRequest request
    ) {
        Long userId = principal != null ? principal.getId() : null;

        // GET CART
        CartEntity cart = cartService.getCart(userId, request.getGuestId());

        if (cart.getItems().isEmpty()) {
            throw new AppException(ErrorCode.CART_EMPTY);
        }

        // CREATE ORDER
        OrderEntity order = new OrderEntity();

        if (userId != null) {
            order.setUserId(userId);
        } else {
            order.setGuestName(request.getGuestName());
            order.setGuestEmail(request.getGuestEmail());
            order.setGuestPhone(request.getGuestPhone());
        }

        order.setShippingAddress(request.getShippingAddress());
        order.setBillingAddress(
                request.getBillingAddress() != null
                        ? request.getBillingAddress()
                        : request.getShippingAddress()
        );

        order.setStatus(OrderStatus.NEW);
        order.setPaymentStatus(PaymentStatus.PENDING);

        // CART → ORDER ITEMS
        List<OrderItemEntity> items = cart.getItems().stream().map(ci -> {
            ProductEntity product = ci.getProduct();

            OrderItemEntity item = new OrderItemEntity();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setQuantity(ci.getQuantity());
            item.setPrice(
                    product.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity()))
            );
            item.setOrder(order);
            return item;
        }).collect(Collectors.toList());

        order.setItems(items);

        BigDecimal total = items.stream()
                .map(OrderItemEntity::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalPrice(total);

        // SAVE ORDER
        OrderEntity saved = orderService.save(order);

        // CLEAR CART
        cart.getItems().clear();
        cartRepository.save(cart);

        // MAP RESPONSE
        return OrderMapper.toResponse(saved);
    }

    // =========================
    // GET USER ORDERS
    // =========================

    /**
     * Lấy tất cả đơn hàng của user đang login
     */
    @GetMapping
    public List<OrderResponse> getUserOrders(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        List<OrderEntity> orders = orderService.getOrdersByUserId(principal.getId());
        return orders.stream()
                .map(OrderMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy chi tiết một đơn hàng cụ thể
     */
    @GetMapping("/{orderId}")
    public OrderResponse getOrderDetail(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long orderId
    ) {
        if (principal == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        OrderEntity order = orderService.getOrderById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        // Kiểm tra xem order có thuộc về user này không
        if (order.getUserId() == null || !order.getUserId().equals(principal.getId())) {
            throw new AppException(ErrorCode.FORBIDDEN, "Bạn không có quyền xem đơn hàng này");
        }

        return OrderMapper.toResponse(order);
    }

    /**
     * Hủy đơn hàng (nếu còn ở trạng thái có thể hủy)
     */
    @PostMapping("/{orderId}/cancel")
    public OrderResponse cancelOrder(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long orderId
    ) {
        if (principal == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        OrderEntity order = orderService.getOrderById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        // Kiểm tra quyền
        if (order.getUserId() == null || !order.getUserId().equals(principal.getId())) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        // Chỉ cho phép hủy khi đơn hàng còn ở trạng thái NEW hoặc CONFIRMED
        if (order.getStatus() != OrderStatus.NEW && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new AppException(ErrorCode.CANNOT_CANCEL_ORDER,
                    "Chỉ có thể hủy đơn hàng khi còn ở trạng thái 'Mới' hoặc 'Đã xác nhận'");
        }

        order.setStatus(OrderStatus.CANCELLED);
        OrderEntity updated = orderService.save(order);

        return OrderMapper.toResponse(updated);
    }
}