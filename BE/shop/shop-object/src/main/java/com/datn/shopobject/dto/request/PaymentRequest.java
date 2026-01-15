package com.datn.shopobject.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

        @NotNull(message = "Order ID is required")
        private Long orderId;

        @NotBlank(message = "Payment method is required")
        private String method;

        private Long userId;

        private String guestId;


}