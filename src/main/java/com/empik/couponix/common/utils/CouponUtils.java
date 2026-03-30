package com.empik.couponix.common.utils;

import lombok.experimental.UtilityClass;

import java.util.Locale;

@UtilityClass
public class CouponUtils {

    public static String normalizeToUpperCase(String str) {
        return str == null ? null : str.trim().toUpperCase(Locale.ROOT);
    }
}
