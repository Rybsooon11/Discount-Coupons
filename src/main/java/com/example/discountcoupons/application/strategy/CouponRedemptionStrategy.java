package com.example.discountcoupons.application.strategy;

import com.example.discountcoupons.domain.model.Coupon;

public interface CouponRedemptionStrategy {

    boolean tryRedeem(Coupon coupon, String userId);

    int computeRemainingUses(Coupon coupon);
}
