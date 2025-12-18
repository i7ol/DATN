package com.datn.shopobject.client;

import com.datn.shopobject.dto.response.UserInfoResponse;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class AuthClientFallback implements AuthClient {

    @Override
    public boolean validateToken(String token) {
        log.warn("Auth service unavailable, fallback to default validation");
        return false;
    }

    @Override
    public UserInfoResponse getCurrentUser(String token) {
        log.warn("Auth service unavailable, fallback to empty user");
        return new UserInfoResponse(); // Trả về object rỗng
    }

    @Override
    public boolean checkPermission(String token, String permission) {
        log.warn("Auth service unavailable, fallback to no permission");
        return false;
    }
}