package com.datn.shopobject.dto.request;

public record PostRequest(
        String title,
        String slug,
        String summary,
        String content,
        Long categoryId,
        String author,
        Boolean active
) {}
