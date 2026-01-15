package com.datn.shopobject.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheckoutItemRequest {

    @NotNull
    private Long productId;

    private Long variantId;

    @Min(1)
    private Integer quantity;
}
