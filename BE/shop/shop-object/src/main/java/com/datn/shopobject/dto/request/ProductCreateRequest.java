package com.datn.shopobject.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductCreateRequest {
    String sku;
    String name;
    String description;
    BigDecimal price;
    BigDecimal importPrice;
    Long categoryId;
    List<MultipartFile> images;
    List<VariantRequest> variants;
}
