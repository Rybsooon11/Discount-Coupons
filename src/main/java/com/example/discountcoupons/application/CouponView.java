package com.example.discountcoupons.application;

import java.time.OffsetDateTime;

public record CouponView(
        String code,
        String countryCode,
        int maxUses,
        int remainingUses,
        OffsetDateTime createdAt
) {}
