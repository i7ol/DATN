package com.datn.shopcms.dto.response;

import java.time.Instant;
import java.time.LocalDateTime;

public record BannerResponse(
        Long id,
        String imageUrl,
        String link,
        String position,
        Integer sortOrder,
        Boolean active,
        Instant createdAt,
        Instant updatedAt
) {}
