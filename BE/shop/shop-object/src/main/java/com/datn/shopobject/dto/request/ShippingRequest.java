
package com.datn.shopobject.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ShippingRequest {
    @NotNull
    private Long orderId;

    private String shippingCompany;
    private String shippingMethod;
    private String trackingNumber;
    private Double shippingFee;
    private Integer estimatedDeliveryDays;
    private String notes;
}