package com.datn.shopdatabase.repository;

import com.datn.shopdatabase.entity.PushTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PushTokenRepository extends JpaRepository<PushTokenEntity, Long> {

    Optional<PushTokenEntity> findByToken(String token);

    // Add this method
    Optional<PushTokenEntity> findByUserId(Long userId);
}

