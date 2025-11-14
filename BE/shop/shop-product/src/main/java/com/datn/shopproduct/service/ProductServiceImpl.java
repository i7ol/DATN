package com.datn.shopproduct.service;

import com.datn.shopcore.exception.AppException;
import com.datn.shopcore.exception.ErrorCode;
import com.datn.shopproduct.dto.ImageDTO;
import com.datn.shopproduct.dto.request.ProductCreateRequest;
import com.datn.shopproduct.dto.request.ProductUpdateRequest;
import com.datn.shopproduct.dto.response.ProductResponse;
import com.datn.shopproduct.entity.Category;
import com.datn.shopproduct.entity.Product;
import com.datn.shopproduct.entity.ProductImage;
import com.datn.shopproduct.repository.CategoryRepository;
import com.datn.shopproduct.repository.ProductRepository;
import com.datn.shopinventory.entity.InventoryItem;
import com.datn.shopinventory.repository.InventoryRepository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductServiceImpl implements ProductService {

    ProductRepository productRepository;
    CategoryRepository categoryRepository;
    InventoryRepository inventoryRepository;

    public ProductServiceImpl(ProductRepository productRepository,
                              CategoryRepository categoryRepository,
                              InventoryRepository inventoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public ProductResponse createProduct(ProductCreateRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setSku(request.getSku());
        product.setPrice(request.getPrice());
        product.setImportPrice(request.getImportPrice());

        // set category
        setCategoryIfPresent(product, request.getCategoryId());

        // set images
        setImagesIfPresent(product, request.getImages());

        // save product
        Product saved = productRepository.save(product);

        // Tự động tạo InventoryItem
        InventoryItem item = new InventoryItem();
        item.setProductId(saved.getId());
        item.setStock(0); // stock mặc định
        item.setReservedQuantity(0);
        item.setSellingPrice(saved.getPrice());
        item.setImportPrice(saved.getImportPrice());
        inventoryRepository.save(item);

        return toProductResponse(saved);
    }

    @Override
    public ProductResponse updateProduct(Long id, ProductUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        if (request.getName() != null) product.setName(request.getName());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getSku() != null) product.setSku(request.getSku());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getImportPrice() != null) product.setImportPrice(request.getImportPrice());

        // update category
        if (request.getCategoryId() != null) {
            setCategoryIfPresent(product, request.getCategoryId());
        } else {
            product.setCategory(null);
        }

        // update images
        replaceImages(product, request.getImages());

        Product updated = productRepository.save(product);

        // Đồng bộ giá vào Inventory nếu tồn tại
        inventoryRepository.findByProductId(updated.getId())
                .ifPresent(item -> {
                    item.setSellingPrice(updated.getPrice());
                    item.setImportPrice(updated.getImportPrice());
                    inventoryRepository.save(item);
                });

        return toProductResponse(updated);
    }

    @Override
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    @Override
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        return toProductResponse(product);
    }

    @Override
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::toProductResponse)
                .collect(Collectors.toList());
    }

    // ==================== Helper methods ====================

    private void setCategoryIfPresent(Product product, Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        product.setCategory(category);
    }

    private void setImagesIfPresent(Product product, List<ImageDTO> imageDTOs) {
        if (imageDTOs != null && !imageDTOs.isEmpty()) {
            List<ProductImage> images = new ArrayList<>();
            for (ImageDTO dto : imageDTOs) {
                ProductImage img = new ProductImage();
                img.setUrl(dto.getUrl());
                img.setProduct(product);
                images.add(img);
            }
            product.setImages(images);
        }
    }

    private void replaceImages(Product product, List<ImageDTO> imageDTOs) {
        if (imageDTOs != null) {
            if (product.getImages() == null) {
                product.setImages(new ArrayList<>());
            } else {
                product.getImages().clear();
            }
            for (ImageDTO dto : imageDTOs) {
                ProductImage img = new ProductImage();
                img.setUrl(dto.getUrl());
                img.setProduct(product);
                product.getImages().add(img);
            }
        }
    }

    private ProductResponse toProductResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setSku(product.getSku());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setImportPrice(product.getImportPrice());

        if (product.getCategory() != null) {
            response.setCategoryId(product.getCategory().getId());
            response.setCategoryName(product.getCategory().getName());
        }

        if (product.getImages() != null) {
            List<ImageDTO> images = product.getImages().stream()
                    .map(img -> new ImageDTO(img.getId(), img.getUrl()))
                    .collect(Collectors.toList());
            response.setImages(images);
        }

        return response;
    }
}
