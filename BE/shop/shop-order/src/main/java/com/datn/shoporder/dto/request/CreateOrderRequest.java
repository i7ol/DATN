package com.datn.shoporder.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    // Nếu guest checkout → userId = null
    private Long userId;

    // Guest info (bắt buộc khi userId null)
    private String guestName;
    private String guestEmail;
    private String guestPhone;
    private String shippingAddress;
    private String billingAddress;

    @NotEmpty(message = "Order items cannot be empty")
    private List<OrderItemRequest> items;
}
