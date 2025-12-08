package com.datn.shopobject.dto.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VariantUpdateRequest {
    private Long id;        // null nếu tạo mới
    private String sizeName;
    private String color;
}
