
    package com.datn.shopapp.controller;
    
    import com.datn.shopapp.config.UserPrincipal;
    import com.datn.shopcart.service.CartService;
    import com.datn.shopdatabase.entity.*;
    import com.datn.shopdatabase.enums.OrderStatus;
    import com.datn.shopdatabase.enums.PaymentStatus;
    import com.datn.shopdatabase.exception.AppException;
    import com.datn.shopdatabase.exception.ErrorCode;
    import com.datn.shopdatabase.repository.InventoryRepository;
    import com.datn.shopobject.dto.request.CheckoutRequest;
    import com.datn.shopobject.dto.response.OrderResponse;
    import com.datn.shoporder.mapper.OrderMapper;
    import com.datn.shoporder.service.OrderService;
    import jakarta.transaction.Transactional;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.security.core.annotation.AuthenticationPrincipal;
    import org.springframework.web.bind.annotation.*;
    
    import java.math.BigDecimal;
    import java.util.List;
    import java.util.stream.Collectors;
    
    @RestController
    @RequestMapping("/api/orders")
    @RequiredArgsConstructor
    @Slf4j
    public class OrderUserController {

        private final OrderService orderService;
        private final CartService cartService;
        private final InventoryRepository inventoryRepository;


        @PostMapping("/checkout")
        @Transactional
        public OrderResponse checkout(
                @AuthenticationPrincipal UserPrincipal principal,
                @RequestBody CheckoutRequest request
        ) {
            Long userId = principal != null ? principal.getId() : null;

            if (userId == null && request.getGuestId() == null) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }

            if (userId == null) {
                if (request.getGuestName() == null
                        || request.getGuestEmail() == null
                        || request.getGuestPhone() == null) {
                    throw new AppException(ErrorCode.INVALID_REQUEST, "Thiếu thông tin khách hàng");
                }
            }

            CartEntity cart = cartService.getCart(userId, request.getGuestId());

            if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
                throw new AppException(ErrorCode.CART_EMPTY);
            }

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

            List<OrderItemEntity> items = cart.getItems().stream().map(ci -> {
                OrderItemEntity item = new OrderItemEntity();

                ProductVariantEntity variant = ci.getVariant();

                InventoryItemEntity inventory = inventoryRepository
                        .findByVariantId(variant.getId())
                        .orElseThrow(() ->
                                new AppException(
                                        ErrorCode.VARIANT_NOT_FOUND,
                                        "Không tìm thấy tồn kho cho variant " + variant.getId()
                                )
                        );


                item.setVariantId(variant.getId());
                item.setProductId(variant.getProduct().getId());
                item.setProductName(variant.getProduct().getName());

                item.setSize(variant.getSizeName());
                item.setColor(variant.getColor());

                item.setUnitPrice(inventory.getSellingPrice());
                item.setQuantity(ci.getQuantity());

                item.setTotalPrice(
                        inventory.getSellingPrice()
                                .multiply(BigDecimal.valueOf(ci.getQuantity()))
                );

                item.setOrder(order);
                return item;
            }).collect(Collectors.toList());


            order.setItems(items);

            BigDecimal total = items.stream()
                    .map(OrderItemEntity::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            order.setTotalPrice(total);

            OrderEntity saved = orderService.save(order);

            if ("VNPAY".equalsIgnoreCase(request.getPaymentMethod())) {
                order.setStatus(OrderStatus.PENDING_PAYMENT);
                order.setPaymentStatus(PaymentStatus.PENDING);
            } else {
                order.setStatus(OrderStatus.NEW);
                order.setPaymentStatus(PaymentStatus.PENDING);
            }


            return OrderMapper.toResponse(saved);
        }


        /**
         * Xóa cart sau khi tạo order (cho COD)
         */
        private void clearCartAfterOrder(Long userId, String guestId) {
            try {
                if (userId != null) {
                    // Xóa cart của logged-in user
                    cartService.clearUserCart(userId);
                    log.info("Cart cleared for user: {}", userId);
                } else if (guestId != null) {
                    // Xóa cart của guest
                    cartService.clearGuestCart(guestId);
                    log.info("Cart cleared for guest: {}", guestId);
                }
            } catch (Exception e) {
                log.error("Error clearing cart: {}", e.getMessage());
                // Không throw exception vì order đã được tạo thành công
            }
        }
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
                @PathVariable("orderId") Long orderId
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
                @PathVariable("orderId") Long orderId
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