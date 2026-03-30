package com.empik.couponix.coupon.repository;

import com.empik.couponix.coupon.entity.CouponEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CouponRepository extends JpaRepository<CouponEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CouponEntity c where c.code = :code")
    Optional<CouponEntity> findByCodeForUpdate(@Param("code") String code);
}