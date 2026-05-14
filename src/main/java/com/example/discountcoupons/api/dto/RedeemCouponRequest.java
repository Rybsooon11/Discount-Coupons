package com.example.discountcoupons.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RedeemCouponRequest(
        @NotBlank
        @Size(min = 3, max = 64)
        @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "code must be alphanumeric, underscore or dash")
        String code,

        @NotBlank
        @Size(min = 1, max = 64)
        @Pattern(regexp = "^[A-Za-z0-9_@.\\-]+$",
                message = "userId may contain only letters, digits, underscore, dash, dot or @")
        String userId
) {}
