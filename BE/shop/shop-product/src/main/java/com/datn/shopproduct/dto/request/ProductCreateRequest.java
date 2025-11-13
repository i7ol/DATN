package com.datn.shopproduct.dto.request;

import java.math.BigDecimal;
import java.util.List;

import com.datn.shopproduct.dto.ImageDTO;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductCreateRequest {
    @NotBlank String sku;
    @NotBlank String name;
    String description;
    @NotNull @DecimalMin("0.0") BigDecimal price;
    @NotNull @Min(0) Integer stock;
    Long categoryId;
    List<ImageDTO> images;

}

