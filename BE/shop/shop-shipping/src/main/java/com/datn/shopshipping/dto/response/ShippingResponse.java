package com.datn.shopshipping.dto.response;

import com.datn.shopshipping.entity.ShippingOrder;
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
    private ShippingOrder.Status status;
    private String createdAt;
    private String updatedAt;
}
