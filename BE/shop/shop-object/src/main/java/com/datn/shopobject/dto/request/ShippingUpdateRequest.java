
package com.datn.shopobject.dto.request;

import lombok.Data;

@Data
public class ShippingUpdateRequest {
    private String trackingNumber;
    private String shippingCompany;
    private String shippingMethod;
    private Double shippingFee;
    private Integer estimatedDeliveryDays;
    private String notes;
}