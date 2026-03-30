package com.empik.couponix.common.validation;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ValidationMessages {

    public static final String COUPON_CODE_INVALID =
            "must contain only letters, digits, underscore or hyphen";

    public static final String COUNTRY_CODE_INVALID =
            "must be a 2-letter country code";
}