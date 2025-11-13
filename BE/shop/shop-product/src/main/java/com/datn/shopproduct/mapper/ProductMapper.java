package com.datn.shopproduct.mapper;


import com.datn.shopproduct.entity.Product;
import com.datn.shopproduct.dto.CategoryDto;
import com.datn.shopproduct.dto.ProductImageDto;
import com.datn.shopproduct.dto.request.ProductCreateRequest;
import com.datn.shopproduct.dto.request.ProductUpdateRequest;
import com.datn.shopproduct.dto.response.ProductResponse;
import com.datn.shopproduct.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    Product toProduct(ProductCreateRequest request);

    void updateProduct(@MappingTarget Product product, ProductUpdateRequest request);


    ProductResponse toProductResponse(Product product);

    default CategoryDto toCategoryDto(Category category) {
        if (category == null) return null;
        return new CategoryDto(category.getId(), category.getName());
    }

    default ProductImageDto toProductImageDto(ProductImage image) {
        if (image == null) return null;
        return new ProductImageDto(image.getId(), image.getUrl());
    }


    default java.util.List<ProductImageDto> toProductImageDtoList(java.util.List<ProductImage> images) {
        if (images == null) return null;
        return images.stream().map(this::toProductImageDto).collect(Collectors.toList());
    }
}
