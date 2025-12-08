package com.datn.shopobject.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    private Long userId;

    private String guestName;
    private String guestEmail;
    private String guestPhone;
    private String shippingAddress;
    private String billingAddress;

    @NotEmpty
    private List<OrderItemRequest> items;
}

