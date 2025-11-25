package com.datn.shopproduct.dto.response;

import com.datn.shopproduct.dto.ImageDTO;
import lombok.*;


import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
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


}
