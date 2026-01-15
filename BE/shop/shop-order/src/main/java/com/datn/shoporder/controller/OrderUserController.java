package com.datn.shoporder.controller;

import com.datn.shopdatabase.entity.OrderEntity;
import com.datn.shopdatabase.entity.OrderItemEntity;
import com.datn.shopdatabase.entity.ProductEntity;
import com.datn.shopdatabase.exception.AppException;
import com.datn.shopdatabase.exception.ErrorCode;
import com.datn.shopdatabase.repository.ProductRepository;
import com.datn.shopobject.dto.request.CheckoutItemRequest;
import com.datn.shopobject.dto.request.CheckoutRequest;
import com.datn.shopobject.dto.response.OrderResponse;
import com.datn.shoporder.mapper.OrderMapper;
import com.datn.shoporder.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.stream.Collectors;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/user/orders")
@RequiredArgsConstructor
public class OrderUserController {

    private final OrderService orderService;
    private final ProductRepository productRepository;

    // ===== CREATE =====
    @PostMapping("/checkout")
    @Transactional
    public OrderResponse checkout(@RequestBody CheckoutRequest request) {

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Danh sách sản phẩm trống");
        }

        List<CheckoutItemRequest> items = request.getItems();

        List<Long> productIds = items.stream()
                .map(CheckoutItemRequest::getProductId)
                .distinct()
                .toList();

        Map<Long, ProductEntity> productMap =
                productRepository.findAllById(productIds)
                        .stream()
                        .collect(Collectors.toMap(ProductEntity::getId, p -> p));

        List<OrderItemEntity> orderItems = items.stream().map(item -> {
            ProductEntity product = productMap.get(item.getProductId());
            if (product == null) {
                throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
            }

            OrderItemEntity entity = new OrderItemEntity();
            entity.setProductId(product.getId());
            entity.setProductName(product.getName());
            entity.setVariantId(item.getVariantId());
            entity.setUnitPrice(product.getPrice());
            entity.setQuantity(item.getQuantity());
            entity.setTotalPrice(
                    product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
            );
            return entity;
        }).toList();

        OrderEntity order = orderService.createOrderFromCheckout(
                request.getUserId(),
                request.getGuestId(),
                request.getGuestName(),
                request.getGuestEmail(),
                request.getGuestPhone(),
                request.getShippingAddress(),
                request.getShippingProvince(),
                request.getShippingDistrict(),
                request.getShippingWard(),
                request.getShippingNote(),
                null, null, null, null,
                orderItems
        );

        return OrderMapper.toResponse(order);
    }




    // ===== READ =====
    @GetMapping("/{orderId}")
    public OrderResponse getOrder(@PathVariable Long orderId) {
        return OrderMapper.toResponse(orderService.getOrder(orderId));
    }

    @GetMapping("/my")
    public List<OrderResponse> myOrders(@RequestParam Long userId) {
        return orderService.getOrdersByUserId(userId)
                .stream()
                .map(OrderMapper::toResponse)
                .toList();
    }

    // ===== CANCEL =====
    @PutMapping("/{orderId}/cancel")
    public OrderResponse cancel(
            @PathVariable Long orderId,
            @RequestParam Long userId
    ) {
        return OrderMapper.toResponse(
                orderService.cancelOrderWithPermission(orderId, userId)
        );
    }
}

