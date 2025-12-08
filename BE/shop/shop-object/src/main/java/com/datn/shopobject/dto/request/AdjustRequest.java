package com.datn.shopobject.dto.request;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class AdjustRequest {
    private Integer newStock;
    private String reason;
}
