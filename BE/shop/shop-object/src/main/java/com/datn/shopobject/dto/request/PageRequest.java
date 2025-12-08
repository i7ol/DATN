package com.datn.shopobject.dto.request;

public record PageRequest(String slug, String title, String summary, String content, String metaTitle, String metaDescription, Boolean active) { }



