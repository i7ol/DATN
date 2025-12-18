// ShippingSummaryResponse.java
package com.datn.shopobject.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShippingSummaryResponse {
    private long total;
    private long preparing;
    private long shipped;
    private long delivered;
    private long cancelled;
}