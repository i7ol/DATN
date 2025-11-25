package com.datn.shopproduct.dto.request;

import java.math.BigDecimal;
import java.util.List;

import com.datn.shopproduct.dto.ImageDTO;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

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
    @NotNull @DecimalMin("0.0") BigDecimal importPrice;
    Long categoryId;
    List<MultipartFile> images;

}

