package com.datn.shopobject.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnItemResponse {

    private Long id;
    private Long productId;
    private String productName;     // Để hiển thị
    private Integer quantity;
    private String reason;
    private List<String> images = new ArrayList<>();
}
