package com.example.discountcoupons.domain.exception;

public class CouponCodeDuplicateException extends RuntimeException {
    public CouponCodeDuplicateException(String code) {
        super("Coupon code already exists: code=" + code);
    }
}
