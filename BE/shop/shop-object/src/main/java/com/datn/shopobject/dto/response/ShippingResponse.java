package com.datn.shopobject.dto.response;

import com.datn.shopdatabase.entity.ShippingOrderEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingResponse {
    private Long id;
    private Long orderId;
    private String recipientName;
    private String recipientPhone;
    private String recipientAddress;
    private String shippingCompany;
    private String shippingMethod;
    private String trackingNumber;
    private Double shippingFee;
    private ShippingOrderEntity.Status status;
    private String createdAt;
    private String updatedAt;
}
