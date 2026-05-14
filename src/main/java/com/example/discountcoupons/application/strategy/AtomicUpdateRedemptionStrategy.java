package com.example.discountcoupons.application.strategy;

import com.example.discountcoupons.domain.model.Coupon;
import com.example.discountcoupons.infrastructure.persistence.CouponRepository;

public class AtomicUpdateRedemptionStrategy implements CouponRedemptionStrategy {

    private final CouponRepository couponRepository;

    public AtomicUpdateRedemptionStrategy(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    @Override
    public boolean tryRedeem(Coupon coupon, String userId) {
        return couponRepository.tryIncrementUsage(coupon.getId()) > 0;
    }

    @Override
    public int computeRemainingUses(Coupon coupon) {
        return couponRepository.findById(coupon.getId())
                .map(c -> c.getMaxUses() - c.getCurrentUses())
                .orElse(0);
    }
}
