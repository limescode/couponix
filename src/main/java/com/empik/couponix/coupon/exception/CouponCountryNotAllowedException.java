package com.empik.couponix.coupon.exception;

public class CouponCountryNotAllowedException extends RuntimeException {

    public CouponCountryNotAllowedException(String code, String countryCode) {
        super("Coupon with code '%s' cannot be used from country '%s'".formatted(code, countryCode));
    }
}