package com.datn.shopnotification.service;

import com.datn.shopnotification.entity.PushToken;
import com.datn.shopnotification.repository.PushTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PushTokenService {

    private final PushTokenRepository repository;

    public void saveOrUpdateToken(Long userId, String token) {
        if (userId == null || token == null || token.isBlank()) return;

        PushToken pushToken = repository.findByUserId(userId)
                .map(existing -> {
                    existing.setToken(token);
                    return existing;
                })
                .orElse(PushToken.builder().userId(userId).token(token).build());

        repository.save(pushToken);
    }

    public String getTokenByUserId(Long userId) {
        return repository.findByUserId(userId).map(PushToken::getToken).orElse(null);
    }
}
