package com.datn.shopobject.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// =====================
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnItemRequest {

    private Long orderItemId;     // ID của item trong đơn hàng gốc
    private Long productId;
    private Integer quantity;
    private String reason;        // Lý do cụ thể cho từng sản phẩm (nếu cần)
}