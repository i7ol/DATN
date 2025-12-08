package com.datn.shopadmin.controller;

import com.datn.shopobject.dto.request.ProductCreateRequest;
import com.datn.shopobject.dto.request.ProductUpdateRequest;
import com.datn.shopobject.dto.request.VariantRequest;
import com.datn.shopobject.dto.request.VariantUpdateRequest;
import com.datn.shopobject.dto.response.ProductResponse;
import com.datn.shopproduct.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class ProductAdminController {

    private final ProductService productService;

    @PostMapping
    public ProductResponse createProduct(
            @RequestPart("product") ProductCreateRequest req,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @RequestPart(value = "variants", required = false) List<VariantRequest> variants
    ) {
        req.setVariants(variants);
        return productService.createProduct(req, files);
    }



    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable("id") Long id,
            @RequestPart("product") ProductUpdateRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @RequestPart(value = "variants", required = false) List<VariantUpdateRequest> variants
    ) {
        request.setVariants(variants); // set variants vào request
        return ResponseEntity.ok(productService.updateProduct(id, request, files));
    }


    @GetMapping
    public Page<ProductResponse> getAllProducts(@RequestParam(value = "page",defaultValue = "0") int page,
                                                @RequestParam(value = "size",defaultValue = "10") int size) {
        Pageable p = PageRequest.of(page, size);
        return productService.getAllProducts(p);
    }

    @GetMapping("/{id}")
    public ProductResponse getProduct(@PathVariable("id") Long id) {
        return productService.getProduct(id);
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable("id") Long id) {
        productService.deleteProduct(id);
    }
}
