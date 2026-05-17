package com.datn.shoporder.mapper;

import com.datn.shopdatabase.entity.OrderReturnEntity;
import com.datn.shopdatabase.entity.OrderReturnItemEntity;
import com.datn.shopdatabase.entity.ReturnImageEntity;
import com.datn.shopobject.dto.response.ReturnItemResponse;
import com.datn.shopobject.dto.response.ReturnResponse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReturnMapper {

    public static ReturnResponse toResponse(OrderReturnEntity entity) {
        if (entity == null) return null;

        ReturnResponse res = new ReturnResponse();

        res.setId(entity.getId());
        res.setOrderId(entity.getOrderId());
        res.setUserId(entity.getUserId());
        res.setGuestId(entity.getGuestId());

        res.setReturnType(entity.getReturnType());
        res.setReason(entity.getReason());
        res.setDescription(entity.getDescription());
        res.setStatus(entity.getStatus());

        res.setRefundAmount(entity.getRefundAmount());
        res.setTotalReturnValue(entity.getTotalReturnValue());
        res.setRefundTransactionId(entity.getRefundTransactionId());
        res.setAdminNote(entity.getAdminNote());
        res.setReturnTrackingCode(entity.getReturnTrackingCode());

        res.setRequestDate(entity.getCreatedAt());
        res.setProcessedDate(entity.getProcessedDate());
        res.setCompletedDate(entity.getCompletedDate());

        // Mapping Items với productName
        if (entity.getItems() != null) {
            res.setItems(
                    entity.getItems().stream()
                            .map(ReturnMapper::toItemResponse)
                            .collect(Collectors.toList())
            );
        } else {
            res.setItems(new ArrayList<>());
        }

        // Mapping Images
        if (entity.getImages() != null) {
            res.setImageUrls(
                    entity.getImages().stream()
                            .map(ReturnImageEntity::getImageUrl)
                            .collect(Collectors.toList())
            );
        } else {
            res.setImageUrls(new ArrayList<>());
        }

        return res;
    }

    public static ReturnItemResponse toItemResponse(OrderReturnItemEntity item) {
        if (item == null) return null;

        ReturnItemResponse dto = new ReturnItemResponse();
        dto.setId(item.getId());
        dto.setProductId(item.getProductId());
        dto.setQuantity(item.getQuantity());
        dto.setReason(item.getReason());
        dto.setProductName(item.getProductName() != null ?
                item.getProductName() : "Sản phẩm #" + item.getProductId());
        dto.setUnitPrice(item.getUnitPrice());

        if (item.getUnitPrice() != null && item.getQuantity() != null) {
            dto.setSubtotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        return dto;
    }

    // Optional: Version dùng cho Admin (có thêm thông tin)
    public static ReturnResponse toAdminResponse(OrderReturnEntity entity) {
        ReturnResponse response = toResponse(entity);
        // Có thể thêm customerName, orderTrackingCode... sau
        return response;
    }
}