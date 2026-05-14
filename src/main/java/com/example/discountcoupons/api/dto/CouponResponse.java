package com.example.discountcoupons.api.dto;

import com.example.discountcoupons.application.CouponView;
import java.time.OffsetDateTime;

public record CouponResponse(
        String code,
        String countryCode,
        int maxUses,
        int remainingUses,
        OffsetDateTime createdAt
) {
    public static CouponResponse from(CouponView view) {
        return new CouponResponse(
                view.code(),
                view.countryCode(),
                view.maxUses(),
                view.remainingUses(),
                view.createdAt()
        );
    }
}
