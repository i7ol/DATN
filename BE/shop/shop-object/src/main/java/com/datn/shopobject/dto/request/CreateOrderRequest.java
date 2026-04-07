package com.datn.shopobject.dto.request;

import com.datn.shopdatabase.enums.PaymentMethod;
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
    private  PaymentMethod paymentMethod;
    @NotEmpty
    private List<OrderItemRequest> items;


}

