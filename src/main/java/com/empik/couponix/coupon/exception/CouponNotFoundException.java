package com.empik.couponix.coupon.exception;

public class CouponNotFoundException extends RuntimeException {

    public CouponNotFoundException(String code) {
        super("Coupon with code '%s' was not found".formatted(code));
    }
}