package com.example.discountcoupons.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateCouponRequest(
        @NotBlank
        @Size(min = 3, max = 64)
        @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "code must be alphanumeric, underscore or dash")
        String code,

        @NotBlank
        @Pattern(regexp = "^[A-Za-z]{2}$", message = "countryCode must be ISO 3166-1 alpha-2")
        String countryCode,

        @Min(1)
        @Max(10_000_000)
        int maxUses
) {}
