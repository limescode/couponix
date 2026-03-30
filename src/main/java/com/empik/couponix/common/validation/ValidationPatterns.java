package com.empik.couponix.common.validation;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ValidationPatterns {

    public static final String COUPON_CODE = "^[A-Za-z0-9_-]+$";
    public static final String COUNTRY_CODE = "^[A-Za-z]{2}$";
}