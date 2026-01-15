package com.datn.shoporder.mapper;

import com.datn.shopdatabase.entity.OrderEntity;
import com.datn.shopobject.dto.response.OrderItemResponse;
import com.datn.shopobject.dto.response.OrderResponse;

import java.util.stream.Collectors;

public class OrderMapper {

    public static OrderResponse toResponse(OrderEntity order) {
        OrderResponse res = new OrderResponse();

        res.setId(order.getId());
        res.setUserId(order.getUserId());
        res.setGuestId(order.getGuestId());
        res.setGuestName(order.getGuestName());
        res.setGuestEmail(order.getGuestEmail());
        res.setGuestPhone(order.getGuestPhone());

        res.setShippingAddress(order.getShippingAddress());
        res.setBillingAddress(order.getBillingAddress());

        res.setTotalPrice(order.getTotalPrice());
        res.setShippingFee(order.getShippingFee());
        res.setDiscountAmount(order.getDiscountAmount());
        res.setFinalAmount(order.getFinalAmount());

        res.setStatus(order.getStatus());
        res.setPaymentStatus(order.getPaymentStatus());
        res.setPaymentMethod(order.getPaymentMethod());
        res.setShippingMethod(order.getShippingMethod());

        res.setCreatedAt(order.getCreatedAt());
        res.setUpdatedAt(order.getUpdatedAt());
        res.setPaymentDate(order.getPaymentDate());
        res.setEstimatedDeliveryDate(order.getEstimatedDeliveryDate());
        res.setActualDeliveryDate(order.getActualDeliveryDate());

        res.setItems(
                order.getItems().stream().map(item -> {
                    OrderItemResponse dto = new OrderItemResponse();
                    dto.setProductId(item.getProductId());
                    dto.setProductName(item.getProductName());
                    dto.setVariantId(item.getVariantId());
                    dto.setSize(item.getSize());
                    dto.setColor(item.getColor());
                    dto.setUnitPrice(item.getUnitPrice());
                    dto.setQuantity(item.getQuantity());
                    dto.setTotalPrice(item.getTotalPrice());
                    return dto;
                }).toList()
        );

        return res;
    }

}
