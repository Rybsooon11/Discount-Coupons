package com.example.discountcoupons.domain.exception;

public class CouponAlreadyUsedException extends RuntimeException {
    public CouponAlreadyUsedException(String code, String userId) {
        super("Coupon already used: code=" + code + ", userId=" + userId);
    }
}
