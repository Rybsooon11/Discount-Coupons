package com.example.discountcoupons.application.strategy;

import com.example.discountcoupons.domain.model.Coupon;

public interface CouponInitializer {

    void initialize(Coupon coupon);
}
