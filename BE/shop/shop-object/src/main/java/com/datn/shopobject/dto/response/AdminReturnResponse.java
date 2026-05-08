package com.datn.shopobject.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;



@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AdminReturnResponse extends ReturnResponse {

    private String customerName;
    private String orderTrackingCode;
    private String shippingAddress;
    private String guestPhone;
    private String guestEmail;
}