package com.datn.shopobject.dto.response;

import com.datn.shopobject.dto.ImageDTO;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {
    private Long id;
    private String sku;
    private String name;
    private String description;

    private BigDecimal price;
    private BigDecimal importPrice;

    private Long categoryId;
    private String categoryName;

    private List<ImageDTO> images;
    private List<String> colors;
    private List<String> sizes;
    private List<VariantResponse> variants;
}
