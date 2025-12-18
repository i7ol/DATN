package com.datn.shopobject.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductUpdateRequest {
    String sku;
    String name;
    String description;

    @DecimalMin("0.0")
    BigDecimal price;

    @DecimalMin("0.0")
    BigDecimal importPrice;

    Long categoryId;

    List<String> images;
    List<Long> deletedImageIds;

    List<VariantUpdateRequest> variants;
}
