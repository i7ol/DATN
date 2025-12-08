package com.datn.shopdatabase.repository;


import com.datn.shopdatabase.entity.PostCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PostCategoryRepository extends JpaRepository<PostCategoryEntity, Long> {
    Optional<PostCategoryEntity> findBySlug(String slug);
}
