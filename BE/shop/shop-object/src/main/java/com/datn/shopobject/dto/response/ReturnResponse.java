package com.datn.shopobject.dto.response;
import com.datn.shopdatabase.enums.ReturnStatus;
import com.datn.shopdatabase.enums.ReturnType;
import lombok.AllArgsConstructor;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.Instant;

import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnResponse {

    private Long id;
    private Long orderId;
    private Long userId;
    private String guestId;

    private ReturnType returnType;
    private String reason;
    private String description;

    private ReturnStatus status;

    private BigDecimal refundAmount;
    private BigDecimal totalReturnValue;
    private String refundTransactionId;

    private String adminNote;
    private String returnTrackingCode;

    private Instant requestDate;
    private Instant processedDate;
    private Instant completedDate;

    private List<ReturnItemResponse> items;
    private List<String> imageUrls;
}
