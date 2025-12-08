package com.datn.shopnotification.service;

import com.datn.shopdatabase.entity.PushTokenEntity;
import com.datn.shopdatabase.repository.PushTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PushTokenService {

    private PushTokenRepository repository;

    public void saveOrUpdateToken(Long userId, String token) {
        if (userId == null || token == null || token.isBlank()) return;

        PushTokenEntity pushToken = repository.findByUserId(userId)
                .map(existing -> {
                    existing.setToken(token);
                    return existing;
                })
                .orElse(PushTokenEntity.builder().userId(userId).token(token).build());

        repository.save(pushToken);
    }

    public String getTokenByUserId(Long userId) {
        return repository.findByUserId(userId).map(PushTokenEntity::getToken).orElse(null);
    }
}
