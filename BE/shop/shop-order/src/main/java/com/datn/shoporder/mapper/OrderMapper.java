package com.datn.shoporder.mapper;

import com.datn.shopdatabase.entity.OrderEntity;
import com.datn.shopdatabase.entity.OrderItemEntity;
import com.datn.shopobject.dto.response.OrderItemResponse;
import com.datn.shopobject.dto.response.OrderResponse;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

public class OrderMapper {

    private OrderMapper() {
        // util class
    }

    // =========================
    // ENTITY → RESPONSE
    // =========================
    public static OrderResponse toResponse(OrderEntity order) {
        if (order == null) return null;

        // Tính final amount nếu chưa có
        if (order.getFinalAmount() == null) {
            order.calculateFinalAmount();
        }

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .guestName(order.getGuestName())
                .guestEmail(order.getGuestEmail())
                .guestPhone(order.getGuestPhone())
                .shippingAddress(buildFullAddress(order))
                .billingAddress(buildBillingAddress(order))
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .totalPrice(order.getTotalPrice())
                .shippingFee(order.getShippingFee())
                .discountAmount(order.getDiscountAmount())
                .finalAmount(order.getFinalAmount())
                .paymentMethod(order.getPaymentMethod())
                .shippingMethod(order.getShippingMethod())
                .items(toItemResponses(order.getItems()))
                .createdAt(convertToLocalDateTime(order.getCreatedAt()))
                .updatedAt(convertToLocalDateTime(order.getUpdatedAt()))
                .paymentDate(convertToLocalDateTime(order.getPaymentDate()))
                .estimatedDeliveryDate(convertToLocalDateTime(order.getEstimatedDeliveryDate()))
                .actualDeliveryDate(convertToLocalDateTime(order.getActualDeliveryDate()))
                .build();
    }

    // =========================
    // ITEM LIST
    // =========================
    private static List<OrderItemResponse> toItemResponses(List<OrderItemEntity> items) {
        if (items == null) return List.of();

        return items.stream()
                .map(OrderMapper::toItemResponse)
                .collect(Collectors.toList());
    }

    // =========================
    // ITEM
    // =========================
    private static OrderItemResponse toItemResponse(OrderItemEntity item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .unitPrice(item.getPrice() != null && item.getQuantity() > 0
                        ? item.getPrice().divide(BigDecimal.valueOf(item.getQuantity()))
                        : BigDecimal.ZERO)
                .createdAt(convertToLocalDateTime(item.getCreatedAt()))
                .updatedAt(convertToLocalDateTime(item.getUpdatedAt()))
                .build();
    }

    // =========================
    // HELPER METHODS
    // =========================
    private static String buildFullAddress(OrderEntity order) {
        StringBuilder address = new StringBuilder();

        if (order.getShippingAddress() != null) {
            address.append(order.getShippingAddress());
        }

        if (order.getShippingWard() != null) {
            if (address.length() > 0) address.append(", ");
            address.append(order.getShippingWard());
        }

        if (order.getShippingDistrict() != null) {
            if (address.length() > 0) address.append(", ");
            address.append(order.getShippingDistrict());
        }

        if (order.getShippingProvince() != null) {
            if (address.length() > 0) address.append(", ");
            address.append(order.getShippingProvince());
        }

        if (order.getShippingNote() != null && !order.getShippingNote().isEmpty()) {
            if (address.length() > 0) address.append(" - ");
            address.append("Ghi chú: ").append(order.getShippingNote());
        }

        return address.toString();
    }

    private static String buildBillingAddress(OrderEntity order) {
        if (order.getBillingAddress() == null) {
            return buildFullAddress(order); // Fallback to shipping address
        }

        StringBuilder address = new StringBuilder(order.getBillingAddress());

        if (order.getBillingWard() != null) {
            address.append(", ").append(order.getBillingWard());
        }

        if (order.getBillingDistrict() != null) {
            address.append(", ").append(order.getBillingDistrict());
        }

        if (order.getBillingProvince() != null) {
            address.append(", ").append(order.getBillingProvince());
        }

        return address.toString();
    }

    private static LocalDateTime convertToLocalDateTime(Instant instant) {
        return instant != null ? LocalDateTime.ofInstant(instant, ZoneId.systemDefault()) : null;
    }
}