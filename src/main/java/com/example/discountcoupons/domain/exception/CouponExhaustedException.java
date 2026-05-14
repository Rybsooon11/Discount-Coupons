package com.example.discountcoupons.domain.exception;

public class CouponExhaustedException extends RuntimeException {
    public CouponExhaustedException(String code) {
        super("Coupon exhausted: code=" + code);
    }
}
