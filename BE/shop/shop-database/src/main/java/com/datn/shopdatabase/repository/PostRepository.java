package com.datn.shopcms.repository;

import com.datn.shopdatabase.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    Optional<Post> findBySlug(String slug);
    Page<Post> findAllByCategoryIdAndActiveTrue(Long categoryId, Pageable pageable);
    Page<Post> findAllByActiveTrue(Pageable pageable);

}
