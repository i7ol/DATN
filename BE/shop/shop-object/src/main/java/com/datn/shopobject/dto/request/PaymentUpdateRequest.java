package com.datn.shopobject.dto.request;


import com.datn.shopdatabase.enums.PaymentStatus;
import lombok.Data;

@Data
public class PaymentUpdateRequest {
    private PaymentStatus paymentStatus;
}

