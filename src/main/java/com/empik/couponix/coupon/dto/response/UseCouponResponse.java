package com.empik.couponix.coupon.dto.response;

public record UseCouponResponse(
        String code,
        String userId,
        String status
) {
}