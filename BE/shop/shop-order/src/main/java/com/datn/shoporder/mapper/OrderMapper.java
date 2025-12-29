package com.datn.shoporder.mapper;

import com.datn.shopdatabase.entity.OrderEntity;
import com.datn.shopobject.dto.response.OrderItemResponse;
import com.datn.shopobject.dto.response.OrderResponse;

import java.util.stream.Collectors;

public class OrderMapper {

    public static OrderResponse toResponse(OrderEntity order) {
        OrderResponse res = new OrderResponse();

        res.setId(order.getId());
        res.setTotalPrice(order.getTotalPrice());
        res.setStatus(order.getStatus());
        res.setPaymentStatus(order.getPaymentStatus());
        res.setCreatedAt(order.getCreatedAt());

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
                }).collect(Collectors.toList())
        );

        return res;
    }
}
