package com.datn.shopapp.controller;

import com.datn.shopobject.dto.response.ProductResponse;
import com.datn.shopproduct.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/products")
@RequiredArgsConstructor
public class ProductUserController {
    private final ProductService productService;

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
}
