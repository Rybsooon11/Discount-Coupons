package com.example.discountcoupons.application;

public interface CouponService {

    CouponView createCoupon(String code, String countryCode, int maxUses);

    RedemptionResult redeem(String code, String userId, String ip);

    record RedemptionResult(String code, int remainingUses) {}
}
