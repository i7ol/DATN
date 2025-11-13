package com.datn.shopproduct.service;



import com.datn.shopproduct.dto.request.ProductCreateRequest;
import com.datn.shopproduct.dto.request.ProductUpdateRequest;
import com.datn.shopproduct.dto.response.ProductResponse;

import java.util.List;

public interface ProductService {
    ProductResponse createProduct(ProductCreateRequest request);
    ProductResponse updateProduct(Long id, ProductUpdateRequest request);
    void deleteProduct(Long id);
    ProductResponse getProductById(Long id);
    List<ProductResponse> getAllProducts();
}

