package com.datn.shopobject.dto.mapper;

import com.datn.shopdatabase.entity.*;
import com.datn.shopobject.dto.ImageDTO;
import com.datn.shopobject.dto.response.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductMapper {

    public ProductResponse toResponse(
            ProductEntity product,
            List<InventoryItemEntity> inventoryItems
    ) {

        List<String> colors = product.getVariants()
                .stream()
                .map(ProductVariantEntity::getColor)
                .distinct()
                .toList();

        List<String> sizes = product.getVariants()
                .stream()
                .map(ProductVariantEntity::getSizeName)
                .distinct()
                .toList();

        List<ImageDTO> images = product.getImages()
                .stream()
                .map(img -> new ImageDTO(img.getId(), img.getUrl()))
                .toList();

        List<VariantResponse> variantResponses = product.getVariants()
                .stream()
                .map(variant -> {

                    InventoryItemEntity inv = inventoryItems.stream()
                            .filter(i -> i.getVariantId().equals(variant.getId()))
                            .findFirst()
                            .orElse(null);

                    return VariantResponse.builder()
                            .id(variant.getId())
                            .sizeName(variant.getSizeName())
                            .color(variant.getColor())
                            .stock(inv != null ? inv.getStock() : 0)
                            .reservedQuantity(inv != null ? inv.getReservedQuantity() : 0)
                            .availableQuantity(inv != null ? inv.getAvailableQuantity() : 0)
                            .importPrice(inv != null ? inv.getImportPrice() : null)
                            .sellingPrice(inv != null ? inv.getSellingPrice() : null)
                            .thumbnail(product.getImages().isEmpty()
                                    ? null
                                    : product.getImages().get(0).getUrl())
                            .build();
                })
                .toList();

        return ProductResponse.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .importPrice(product.getImportPrice())
                .categoryId(
                        product.getCategory() != null ? product.getCategory().getId() : null
                )
                .categoryName(
                        product.getCategory() != null ? product.getCategory().getName() : null
                )
                .images(images)
                .colors(colors)
                .sizes(sizes)
                .variants(variantResponses)
                .build();
    }
}
