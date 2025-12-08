
package com.datn.shopproduct.service;

import com.datn.shopdatabase.entity.*;
import com.datn.shopdatabase.exception.AppException;
import com.datn.shopdatabase.exception.ErrorCode;
import com.datn.shopdatabase.minio.MinioChannel;
import com.datn.shopdatabase.repository.*;
import com.datn.shopobject.dto.ImageDTO;
import com.datn.shopobject.dto.request.ProductCreateRequest;
import com.datn.shopobject.dto.request.ProductUpdateRequest;
import com.datn.shopobject.dto.request.VariantRequest;
import com.datn.shopobject.dto.request.VariantUpdateRequest;
import com.datn.shopobject.dto.response.ProductResponse;
import com.datn.shopobject.dto.response.VariantResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductImageRepository productImageRepository;
    private final InventoryRepository inventoryRepository;
    private final MinioChannel minioChannel;

    // ================== CREATE PRODUCT ==================
    @Override
    public ProductResponse createProduct(ProductCreateRequest request, List<MultipartFile> files) {
        ProductEntity product = new ProductEntity();
        product.setName(request.getName());
        product.setSku(request.getSku());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setImportPrice(request.getImportPrice());

        if (request.getCategoryId() != null) {
            CategoryEntity category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
            product.setCategory(category);
        }

        ProductEntity saved = productRepository.save(product);

        uploadImages(saved, files);

        if (request.getVariants() != null) {
            for (VariantRequest v : request.getVariants()) {
                ProductVariantEntity variant = createVariant(saved, v.getSizeName(), v.getColor());
                saved.getVariants().add(variant);
            }
        }

        return toResponse(saved);
    }

    // ================== UPDATE PRODUCT ==================
    @Override
    public ProductResponse updateProduct(Long id, ProductUpdateRequest request, List<MultipartFile> files) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        updateBasicInfo(product, request);
        // Xóa các ảnh cũ nếu có
        if (request.getDeletedImageIds() != null) {
            for (Long imgId : request.getDeletedImageIds()) {
                // Xóa khỏi collection ProductEntity
                product.getImages().removeIf(img -> img.getId().equals(imgId));
                // Xóa khỏi DB + MinIO
                productImageRepository.findById(imgId).ifPresent(img -> {
                    minioChannel.delete(img.getUrl()); // xóa file trong MinIO
                    productImageRepository.delete(img);
                });
            }
        }

        uploadImages(product, files);
        handleVariants(product, request.getVariants());

        ProductEntity saved = productRepository.save(product);
        return toResponse(saved);
    }

    // ================== DELETE PRODUCT ==================
    @Override
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    // ================== GET PRODUCT ==================
    @Override
    public ProductResponse getProduct(Long id) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        return toResponse(product);
    }

    @Override
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(this::toResponse);
    }

    // ================== HELPER METHODS ==================

    private void updateBasicInfo(ProductEntity product, ProductUpdateRequest request) {
        if (request.getName() != null) product.setName(request.getName());
        if (request.getSku() != null) product.setSku(request.getSku());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getImportPrice() != null) product.setImportPrice(request.getImportPrice());

        if (request.getCategoryId() != null) {
            CategoryEntity category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
            product.setCategory(category);
        }
    }

    private void uploadImages(ProductEntity product, List<MultipartFile> files) {
        if (files != null && !files.isEmpty()) {
            if (product.getImages() == null) product.setImages(new ArrayList<>());
            for (MultipartFile file : files) {
                String url = minioChannel.upload(file);
                ProductImageEntity img = new ProductImageEntity();
                img.setUrl(url);
                img.setProduct(product);
                product.getImages().add(img);
            }
        }
    }

    private ProductVariantEntity createVariant(ProductEntity product, String sizeName, String color) {
        ProductVariantEntity variant = new ProductVariantEntity();
        variant.setSizeName(sizeName);
        variant.setColor(color);
        variant.setProduct(product);
        variantRepository.save(variant);

        InventoryItemEntity inv = new InventoryItemEntity();
        inv.setVariantId(variant.getId());
        inv.setStock(0);
        inv.setReservedQuantity(0);
        inv.setImportPrice(product.getImportPrice());
        inv.setSellingPrice(product.getPrice());
        inventoryRepository.save(inv);

        return variant;
    }

    private void updateInventory(ProductVariantEntity variant, ProductEntity product) {
        inventoryRepository.findByVariantId(variant.getId()).ifPresent(inv -> {
            inv.setImportPrice(product.getImportPrice());
            inv.setSellingPrice(product.getPrice());
            inventoryRepository.save(inv);
        });
    }

    private void handleVariants(ProductEntity product, List<VariantUpdateRequest> incoming) {
        Map<Long, ProductVariantEntity> oldMap = product.getVariants().stream()
                .collect(Collectors.toMap(ProductVariantEntity::getId, v -> v));

        List<VariantUpdateRequest> updates = incoming != null ? incoming : new ArrayList<>();

        for (VariantUpdateRequest vr : updates) {
            if (vr.getId() == null) {
                ProductVariantEntity newVar = createVariant(product, vr.getSizeName(), vr.getColor());
                product.getVariants().add(newVar);
            } else {
                ProductVariantEntity exist = oldMap.get(vr.getId());
                if (exist != null) {
                    exist.setSizeName(vr.getSizeName());
                    exist.setColor(vr.getColor());
                    variantRepository.save(exist);
                    updateInventory(exist, product);
                    oldMap.remove(vr.getId());
                }
            }
        }

        // Xóa những variant còn lại
        for (ProductVariantEntity toDelete : oldMap.values()) {
            product.getVariants().remove(toDelete);
            inventoryRepository.findByVariantId(toDelete.getId())
                    .ifPresent(inventoryRepository::delete);
            variantRepository.delete(toDelete);
        }
    }

    private ProductResponse toResponse(ProductEntity product) {
        ProductResponse res = new ProductResponse();
        res.setId(product.getId());
        res.setName(product.getName());
        res.setSku(product.getSku());
        res.setDescription(product.getDescription());
        res.setPrice(product.getPrice());
        res.setImportPrice(product.getImportPrice());

        if (product.getCategory() != null) {
            res.setCategoryId(product.getCategory().getId());
            res.setCategoryName(product.getCategory().getName());
        }

        if (product.getImages() != null && !product.getImages().isEmpty()) {
            res.setImages(product.getImages().stream()
                    .map(img -> new ImageDTO(img.getId(), img.getUrl()))
                    .collect(Collectors.toList()));
        }

        if (product.getVariants() != null && !product.getVariants().isEmpty()) {
            List<VariantResponse> variants = product.getVariants().stream().map(v -> {
                VariantResponse vr = new VariantResponse();
                vr.setId(v.getId());
                vr.setSizeName(v.getSizeName());
                vr.setColor(v.getColor());
                inventoryRepository.findByVariantId(v.getId()).ifPresent(inv -> {
                    vr.setStock(inv.getStock());
                    vr.setAvailableQuantity(inv.getAvailableQuantity());
                    vr.setImportPrice(inv.getImportPrice());
                    vr.setSellingPrice(inv.getSellingPrice());
                });
                return vr;
            }).collect(Collectors.toList());
            res.setVariants(variants);
        }

        return res;
    }
}
