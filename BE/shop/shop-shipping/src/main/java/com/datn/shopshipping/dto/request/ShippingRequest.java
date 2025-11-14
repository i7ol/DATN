package com.datn.shopshipping.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingRequest {
    private Long orderId;
    private String shippingCompany;
    private String shippingMethod;
    private String trackingNumber;
    private Double shippingFee;
}
