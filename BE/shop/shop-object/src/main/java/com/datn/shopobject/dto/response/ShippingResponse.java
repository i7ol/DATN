
package com.datn.shopobject.dto.response;


import com.datn.shopdatabase.enums.StatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingResponse {
    private Long id;
    private Long orderId;
    private Long userId;
    private String recipientName;
    private String recipientPhone;
    private String recipientEmail;
    private String recipientAddress;
    private String shippingCompany;
    private String shippingMethod;
    private String trackingNumber;
    private Double shippingFee;
    private Integer estimatedDeliveryDays;
    private LocalDate estimatedDeliveryDate;
    private StatusEnum status;
    private String currentLocation;
    private String notes;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime lastSyncAt;
    private String createdAt;
    private String updatedAt;
}