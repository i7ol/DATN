
package com.datn.shopobject.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PaymentSummaryResponse {
    private BigDecimal totalAmount;
    private BigDecimal pendingAmount;
    private long totalTransactions;
    private long successfulTransactions;
}