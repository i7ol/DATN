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
import com.datn.shopproduct.minio.MinioChannel;
import com.datn.shopproduct.repository.CategoryRepository;
import com.datn.shopproduct.repository.ProductRepository;
import com.datn.shopinventory.entity.InventoryItem;
import com.datn.shopinventory.repository.InventoryRepository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductServiceImpl implements ProductService {

    ProductRepository productRepository;
    CategoryRepository categoryRepository;
    InventoryRepository inventoryRepository;
    MinioChannel minioChannel;

    public ProductServiceImpl(ProductRepository productRepository,
                              CategoryRepository categoryRepository,
                              InventoryRepository inventoryRepository,
                              MinioChannel minioChannel) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.inventoryRepository = inventoryRepository;
        this.minioChannel = minioChannel;
    }

    // ====================================================
    // CREATE PRODUCT
    // ====================================================
    @Override
    public ProductResponse createProduct(ProductCreateRequest request, List<MultipartFile> files) {
        Product product = new Product();
        product.setName(request.getName());
        product.setSku(request.getSku());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setImportPrice(request.getImportPrice());

        // Set category
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        product.setCategory(category);

        // Save product để có ID
        Product saved = productRepository.save(product);

        // Upload nhiều ảnh, dùng biến final để lambda
        if (files != null && !files.isEmpty()) {
            final Product finalSaved = saved; // biến final cho lambda
            List<ProductImage> images = files.stream()
                    .map(file -> {
                        String url = minioChannel.upload(file);
                        ProductImage img = new ProductImage();
                        img.setUrl(url);
                        img.setProduct(finalSaved);
                        return img;
                    })
                    .collect(Collectors.toList());
            finalSaved.setImages(images);
            saved = productRepository.save(finalSaved);
        }

        // Tạo inventory
        InventoryItem item = new InventoryItem();
        item.setProductId(saved.getId());
        item.setStock(0);
        item.setReservedQuantity(0);
        item.setSellingPrice(saved.getPrice());
        item.setImportPrice(saved.getImportPrice());
        inventoryRepository.save(item);

        return toResponse(saved);
    }

    @Override
    public ProductResponse updateProduct(Long id, ProductUpdateRequest request, List<MultipartFile> files) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        if (request.getName() != null) product.setName(request.getName());
        if (request.getSku() != null) product.setSku(request.getSku());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getImportPrice() != null) product.setImportPrice(request.getImportPrice());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
            product.setCategory(category);
        }

        // Thêm ảnh mới nhưng giữ ảnh cũ
        if (files != null && !files.isEmpty()) {
            files.forEach(file -> {
                String url = minioChannel.upload(file);
                ProductImage img = new ProductImage();
                img.setUrl(url);
                img.setProduct(product);
                product.getImages().add(img);
            });
        }

        Product updated = productRepository.save(product);

        // Đồng bộ giá inventory
        inventoryRepository.findByProductId(updated.getId()).ifPresent(item -> {
            item.setSellingPrice(updated.getPrice());
            item.setImportPrice(updated.getImportPrice());
            inventoryRepository.save(item);
        });

        return toResponse(updated);
    }



    // ====================================================
    // DELETE PRODUCT
    // ====================================================
    @Override
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    // ====================================================
    // GET PRODUCT BY ID
    // ====================================================
    @Override
    public ProductResponse getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        return toResponse(product);
    }

    // ====================================================
    // GET ALL PRODUCTS (Pageable)
    // ====================================================
    @Override
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(this::toResponse);
    }

    // ====================================================
    // HELPER: Convert Product -> ProductResponse
    // ====================================================

    private ProductResponse toResponse(Product product) {
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
            List<ImageDTO> images = product.getImages().stream()
                    .map(img -> new ImageDTO(img.getId(), img.getUrl()))
                    .collect(Collectors.toList());
            res.setImages(images);
        }

        return res;
    }
}
