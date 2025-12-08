package com.datn.shopobject.dto.response;

import java.time.Instant;


public record PostResponse(
        Long id,
        String title,
        String slug,
        String summary,
        String content,
        Long categoryId,
        String author,
        Boolean active,
        Instant publishedAt,
        Instant createdAt,
        Instant updatedAt
) {}
