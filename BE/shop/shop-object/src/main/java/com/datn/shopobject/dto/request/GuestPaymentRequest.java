package com.datn.shopobject.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GuestPaymentRequest {
    @NotNull
    private Long orderId;

    @NotBlank
    private String method;

    @NotBlank
    private String guestId;
}
