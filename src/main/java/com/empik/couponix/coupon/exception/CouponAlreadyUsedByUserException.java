package com.empik.couponix.coupon.exception;

public class CouponAlreadyUsedByUserException extends RuntimeException {

    public CouponAlreadyUsedByUserException(String code, String userId) {
        super("Coupon with code '%s' has already been used by user '%s'".formatted(code, userId));
    }
}