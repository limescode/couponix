package com.empik.couponix.coupon.repository;

import com.empik.couponix.coupon.entity.CouponUsageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CouponUsageRepository extends JpaRepository<CouponUsageEntity, UUID> {

    boolean existsByCoupon_IdAndUserId(UUID couponId, String userId);
}