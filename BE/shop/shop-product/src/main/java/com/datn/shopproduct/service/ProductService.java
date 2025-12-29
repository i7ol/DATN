package com.datn.shopproduct.service;

import com.datn.shopobject.dto.request.ProductCreateRequest;
import com.datn.shopobject.dto.request.ProductUpdateRequest;
import com.datn.shopobject.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {

    ProductResponse createProduct(ProductCreateRequest req, List<MultipartFile> files);

    ProductResponse updateProduct(Long id, ProductUpdateRequest req, List<MultipartFile> files);

    ProductResponse getProduct(Long id);

    Page<ProductResponse> getAllProducts(Pageable pageable);

    void deleteProduct(Long id);
}