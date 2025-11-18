package com.datn.shopcms.repository;

import com.datn.shopcms.entity.PostCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PostCategoryRepository extends JpaRepository<PostCategory, Long> {
    Optional<PostCategory> findBySlug(String slug);
}
