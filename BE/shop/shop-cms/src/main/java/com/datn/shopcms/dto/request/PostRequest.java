package com.datn.shopcms.dto.request;

public record PostRequest(
        String title,
        String slug,
        String summary,
        String content,
        Long categoryId,
        String author,
        Boolean active
) {}
