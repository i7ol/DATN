package com.datn.shopproduct.service;

import com.datn.shopproduct.dto.request.ProductCreateRequest;
import com.datn.shopproduct.dto.request.ProductUpdateRequest;
import com.datn.shopproduct.dto.response.ProductResponse;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {

    ProductResponse createProduct(ProductCreateRequest request, List<MultipartFile> files);

    ProductResponse updateProduct(Long id, ProductUpdateRequest request, List<MultipartFile> files);

    void deleteProduct(Long id);


    Page<ProductResponse> getAllProducts(Pageable pageable);


    ProductResponse getProduct(Long id);
}
