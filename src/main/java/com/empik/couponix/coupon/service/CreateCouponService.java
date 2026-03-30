package com.empik.couponix.coupon.service;

import com.empik.couponix.common.time.TimeProvider;
import com.empik.couponix.coupon.dto.request.CreateCouponRequest;
import com.empik.couponix.coupon.dto.response.CreateCouponResponse;
import com.empik.couponix.coupon.entity.CouponEntity;
import com.empik.couponix.coupon.exception.CouponAlreadyExistsException;
import com.empik.couponix.coupon.mapper.CouponMapper;
import com.empik.couponix.coupon.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.empik.couponix.common.utils.CouponUtils.normalizeToUpperCase;

@Service
@RequiredArgsConstructor
public class CreateCouponService {

    private final CouponRepository couponRepository;
    private final TimeProvider timeProvider;
    private final CouponMapper couponMapper;

    @Transactional
    public CreateCouponResponse createCoupon(CreateCouponRequest request) {
        String normalizedCouponCode = normalizeToUpperCase(request.code());
        String normalizedCountryCode = normalizeToUpperCase(request.countryCode());
        CouponEntity coupon = couponMapper.toNewEntity(
                request,
                normalizedCouponCode,
                normalizedCountryCode,
                timeProvider.now()
        );
        try {
            CouponEntity savedCoupon = couponRepository.save(coupon);
            return couponMapper.toCreateCouponResponse(savedCoupon);
        } catch (DataIntegrityViolationException ex) {
            throw new CouponAlreadyExistsException(normalizedCouponCode);
        }
    }
}