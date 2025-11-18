package com.datn.shopcms.repository;

import com.datn.shopcms.entity.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PageRepository extends JpaRepository<Page, Long> {
    Optional<Page> findBySlug(String slug);
}
