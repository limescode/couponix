package com.empik.couponix.coupon.mapper;

import com.empik.couponix.coupon.dto.request.CreateCouponRequest;
import com.empik.couponix.coupon.dto.response.CreateCouponResponse;
import com.empik.couponix.coupon.entity.CouponEntity;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class CouponMapper {

    public CouponEntity toNewEntity(CreateCouponRequest request, String normalizedCode, String normalizedCountryCode, Instant createdAt) {
        return CouponEntity.builder()
                .code(normalizedCode)
                .createdAt(createdAt)
                .maxUsages(request.maxUsages())
                .currentUsages(0)
                .countryCode(normalizedCountryCode)
                .build();
    }

    public CreateCouponResponse toCreateCouponResponse(CouponEntity coupon) {
        return new CreateCouponResponse(
                coupon.getId(),
                coupon.getCode(),
                coupon.getCreatedAt(),
                coupon.getMaxUsages(),
                coupon.getCurrentUsages(),
                coupon.getCountryCode()
        );
    }
}