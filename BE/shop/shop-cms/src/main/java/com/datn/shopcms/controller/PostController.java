//package com.datn.shopcms.controller;
//
//
//import com.datn.shopobject.dto.request.PostRequest;
//import com.datn.shopobject.dto.response.PostResponse;
//import com.datn.shopcms.service.PostService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/cms/posts")
//@RequiredArgsConstructor
//public class PostController {
//
//    private final PostService postService;
//
//    // Lấy bài viết theo slug
//    @GetMapping("/slug/{slug}")
//    public PostResponse getBySlug(@PathVariable("slug") String slug) {
//        return postService.getBySlug(slug);
//    }
//
//    // Lấy bài viết theo category với paging
//    @GetMapping("/category/{categoryId}")
//    public Page<PostResponse> getByCategory(@PathVariable("categoryId") Long categoryId, Pageable pageable) {
//        return postService.getByCategory(categoryId, pageable);
//    }
//
//    @GetMapping
//    public Page<PostResponse> getAllActive(Pageable pageable) {
//        return postService.getAllActive(pageable);
//    }
//
//    // ADMIN endpoints
//    @PreAuthorize("hasRole('ADMIN')")
//    @PostMapping("/admin")
//    public PostResponse create(@RequestBody PostRequest req) {
//        return postService.create(req);
//    }
//
//    @PreAuthorize("hasRole('ADMIN')")
//    @PutMapping("/admin/{id}")
//    public PostResponse update(@PathVariable("id") Long id, @RequestBody PostRequest req) {
//        return postService.update(id, req);
//    }
//
//    @PreAuthorize("hasRole('ADMIN')")
//    @DeleteMapping("/admin/{id}")
//    public void delete(@PathVariable("id") Long id) {
//        postService.delete(id);
//    }
//}
