package com.datn.shopobject.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

@Data
public class CheckoutRequest {
    private Long userId;
    private String guestId;

    private String guestName;

    @Email(message = "Email không hợp lệ")
    private String guestEmail;

    @Pattern(regexp = "(84|0[3|5|7|8|9])+([0-9]{8})\\b", message = "Số điện thoại không hợp lệ")
    private String guestPhone;

    @NotBlank(message = "Địa chỉ giao hàng không được để trống")
    private String shippingAddress;

    private String shippingProvince;
    private String shippingDistrict;
    private String shippingWard;
    private String shippingNote;

    private String billingAddress;
    private String billingProvince;
    private String billingDistrict;
    private String billingWard;

    private String paymentMethod;
    private String shippingMethod;
    @Valid
    private List<CheckoutItemRequest> items;
}