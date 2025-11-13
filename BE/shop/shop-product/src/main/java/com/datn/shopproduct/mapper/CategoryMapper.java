package com.datn.shopproduct.mapper;



import com.datn.shopproduct.dto.request.CategoryCreateRequest;
import com.datn.shopproduct.dto.request.CategoryUpdateRequest;
import com.datn.shopproduct.dto.response.CategoryResponse;
import com.datn.shopproduct.entity.Category;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    Category toCategory(CategoryCreateRequest request);
    CategoryResponse toCategoryResponse(Category category);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCategory(@MappingTarget Category category, CategoryUpdateRequest request);
}

