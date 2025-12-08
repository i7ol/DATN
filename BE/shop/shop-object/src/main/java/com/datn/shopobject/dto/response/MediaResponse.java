package com.datn.shopobject.dto.response;

import java.time.Instant;

public record MediaResponse(
        Long id,
        String fileName,
        String url,
        String contentType,
        Long size,
        Boolean active,
        Instant createdAt,
        Instant updatedAt
) {}
