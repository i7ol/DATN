package com.datn.shopobject.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MediaRequest(
        @NotBlank String fileName,
        @NotBlank String url,
        @NotBlank String contentType,
        @NotNull Long size,
        @NotNull Boolean active
) {}
