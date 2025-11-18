package com.datn.shopcms.dto.request;

public record BannerRequest(
        String imageUrl,
        String link,
        String position,
        Integer sortOrder,
        Boolean active
) {}
