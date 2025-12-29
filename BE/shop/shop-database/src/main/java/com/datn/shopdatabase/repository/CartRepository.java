package com.datn.shopdatabase.repository;

import com.datn.shopdatabase.entity.CartEntity;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<CartEntity, Long> {

    Optional<CartEntity> findByUserId(Long userId);

    Optional<CartEntity> findByGuestId(String guestId);

    void deleteByUserId(Long userId);

    void deleteByGuestId(String guestId);
}


