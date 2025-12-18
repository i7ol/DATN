
package com.datn.shopobject.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShippingCalculateResponse {
    private String company;
    private String service;
    private Double fee;
    private Integer estimatedDays;
}