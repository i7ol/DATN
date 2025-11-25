package com.datn.shopproduct.controller;

import com.datn.shopproduct.dto.request.ProductCreateRequest;
import com.datn.shopproduct.dto.request.ProductUpdateRequest;
import com.datn.shopproduct.dto.response.ProductResponse;
import com.datn.shopproduct.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ObjectMapper objectMapper; // dùng để parse JSON từ string

    // =======================
    // CREATE PRODUCT
    // =======================
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProductResponse createProduct(
            @RequestPart("product") String productJson,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) throws Exception {
        ProductCreateRequest request = objectMapper.readValue(productJson, ProductCreateRequest.class);
        return productService.createProduct(request, files);
    }

    // =======================
    // UPDATE PRODUCT
    // =======================
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProductResponse updateProduct(
            @PathVariable("id") Long id,
            @RequestPart("product") String productJson,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) throws Exception {
        ProductUpdateRequest request = objectMapper.readValue(productJson, ProductUpdateRequest.class);
        return productService.updateProduct(id, request, files);
    }

    // =======================
    // DELETE PRODUCT
    // =======================
    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable("id") Long id) {
        productService.deleteProduct(id);
    }

    // =======================
    // GET ALL PRODUCTS
    // =======================
    @GetMapping
    public Page<ProductResponse> getAllProducts(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return productService.getAllProducts(pageable);
    }

    // =======================
    // GET PRODUCT BY ID
    // =======================
    @GetMapping("/{id}")
    public ProductResponse getProductById(@PathVariable("id") Long id) {
        return productService.getProduct(id);
    }
}
