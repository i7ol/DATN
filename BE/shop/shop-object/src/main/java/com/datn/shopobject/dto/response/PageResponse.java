package com.datn.shopobject.dto.response;

import java.time.Instant;

public record PageResponse(Long id, String slug, String title, String summary, String content, String metaTitle, String metaDescription, Boolean active, Instant createdAt, Instant updatedAt) { }
