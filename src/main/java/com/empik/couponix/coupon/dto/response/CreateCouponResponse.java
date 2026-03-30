package com.empik.couponix.coupon.dto.response;

import java.time.Instant;
import java.util.UUID;

public record CreateCouponResponse(
        UUID id,
        String code,
        Instant createdAt,
        int maxUsages,
        int currentUsages,
        String countryCode
) {
}