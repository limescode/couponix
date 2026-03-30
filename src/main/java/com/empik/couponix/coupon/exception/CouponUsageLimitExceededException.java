package com.empik.couponix.coupon.exception;

public class CouponUsageLimitExceededException extends RuntimeException {

    public CouponUsageLimitExceededException(String code) {
        super("Coupon with code '%s' has reached maximum number of usages".formatted(code));
    }
}