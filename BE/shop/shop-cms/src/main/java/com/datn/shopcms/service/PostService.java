package com.datn.shopcms.service;

import com.datn.shopobject.dto.request.PostRequest;
import com.datn.shopobject.dto.response.PostResponse;
import com.datn.shopdatabase.entity.PostEntity;
import com.datn.shopdatabase.entity.PostCategoryEntity;
import com.datn.shopdatabase.repository.PostCategoryRepository;
import com.datn.shopdatabase.repository.PostRepository;
import com.datn.shopdatabase.exception.AppException;
import com.datn.shopdatabase.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostService {

    private  PostRepository postRepository;
    private  PostCategoryRepository categoryRepository;

    // Tạo bài viết
    public PostResponse create(PostRequest req) {
        if (postRepository.findBySlug(req.slug()).isPresent()) {
            throw new AppException(ErrorCode.SLUG_EXISTED);
        }

        PostCategoryEntity category = categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

            PostEntity post = PostEntity.builder()
                .title(req.title())
                .slug(req.slug())
                .summary(req.summary())
                .content(req.content())
                .category(category)
                .author(req.author())
                .active(req.active() != null ? req.active() : true)
                .build();

        PostEntity saved = postRepository.save(post);
        return map(saved);
    }

    // Cập nhật bài viết
    public PostResponse update(Long id, PostRequest req) {
        PostEntity post = postRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        PostCategoryEntity category = categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        post.setTitle(req.title());
        post.setSlug(req.slug());
        post.setSummary(req.summary());
        post.setContent(req.content());
        post.setCategory(category);
        post.setAuthor(req.author());
        post.setActive(req.active() != null ? req.active() : post.getActive());

        postRepository.save(post);
        return map(post);
    }

    // Xóa bài viết
    public void delete(Long id) {
        PostEntity post = postRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        postRepository.delete(post);
    }

    // Lấy bài viết theo slug
    public PostResponse getBySlug(String slug) {
        PostEntity post = postRepository.findBySlug(slug)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        return map(post);
    }

    // Lấy bài viết theo category
    public Page<PostResponse> getByCategory(Long categoryId, Pageable pageable) {
        return postRepository.findAllByCategoryIdAndActiveTrue(categoryId, pageable)
                .map(this::map);
    }

    // Lấy tất cả bài viết active
    public Page<PostResponse> getAllActive(Pageable pageable) {
        return postRepository.findAllByActiveTrue(pageable)
                .map(this::map);
    }

    // Mapping entity -> DTO
    private PostResponse map(PostEntity p) {
        return new PostResponse(
                p.getId(),
                p.getTitle(),
                p.getSlug(),
                p.getSummary(),
                p.getContent(),
                p.getCategory() != null ? p.getCategory().getId() : null,
                p.getAuthor(),
                p.getActive(),
                p.getPublishedAt(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}
