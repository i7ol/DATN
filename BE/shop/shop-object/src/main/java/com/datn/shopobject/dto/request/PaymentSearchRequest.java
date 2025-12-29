package com.datn.shopobject.dto.request;

import com.datn.shopdatabase.enums.PaymentMethod;
import com.datn.shopdatabase.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class PaymentSearchRequest {
    private String status;
    private String method;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fromDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate toDate;

    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private String transactionId;
    private Long userId;
    private Long orderId;
}