package com.datn.shopapp.controller;

import com.datn.shopobject.dto.response.ProductResponse;
import com.datn.shopproduct.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/products")
@RequiredArgsConstructor
public class ProductUserController {
    private final ProductService productService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<ProductResponse> getAllProducts(@RequestParam(value = "page",defaultValue = "0") int page,
                                                @RequestParam(value = "size",defaultValue = "10") int size) {
        Pageable p = PageRequest.of(page, size);
        return productService.getAllProducts(p);
    }

    @GetMapping(value = "/api/products/search",produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<ProductResponse> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            Pageable pageable
    ) {
        return productService.searchProducts(keyword, categoryId, pageable);
    }
    @GetMapping(value = "/{id}",produces = MediaType.APPLICATION_JSON_VALUE)
    public ProductResponse getProduct(@PathVariable("id") Long id) {
        return productService.getProduct(id);
    }
}
