package com.datn.shopnotification.repository;

import com.datn.shopnotification.entity.PushToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PushTokenRepository extends JpaRepository<PushToken, Long> {

    Optional<PushToken> findByToken(String token);

    // Add this method
    Optional<PushToken> findByUserId(Long userId);
}

