package com.datn.shopobject.dto.request;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class ExportRequest {
    private Long variantId;
    private Integer quantity;
    private String note;
}
