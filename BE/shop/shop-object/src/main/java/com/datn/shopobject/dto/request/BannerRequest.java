package com.datn.shopobject.dto.request;

public record BannerRequest(
        String imageUrl,
        String link,
        String position,
        Integer sortOrder,
        Boolean active
) {}
