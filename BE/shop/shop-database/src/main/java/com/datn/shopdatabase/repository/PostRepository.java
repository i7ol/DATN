package com.datn.shopdatabase.repository;


import com.datn.shopdatabase.entity.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface PostRepository extends JpaRepository<PostEntity, Long> {
    Optional<PostEntity> findBySlug(String slug);
    Page<PostEntity> findAllByCategoryIdAndActiveTrue(Long categoryId, Pageable pageable);
    Page<PostEntity> findAllByActiveTrue(Pageable pageable);

}
