package com.empik.couponix.coupon.service;

import com.empik.couponix.common.time.TimeProvider;
import com.empik.couponix.coupon.dto.request.UseCouponRequest;
import com.empik.couponix.coupon.dto.response.UseCouponResponse;
import com.empik.couponix.coupon.entity.CouponEntity;
import com.empik.couponix.coupon.entity.CouponUsageEntity;
import com.empik.couponix.coupon.exception.CouponAlreadyUsedByUserException;
import com.empik.couponix.coupon.exception.CouponCountryNotAllowedException;
import com.empik.couponix.coupon.exception.CouponNotFoundException;
import com.empik.couponix.coupon.exception.CouponUsageLimitExceededException;
import com.empik.couponix.coupon.repository.CouponRepository;
import com.empik.couponix.coupon.repository.CouponUsageRepository;
import com.empik.couponix.geolocation.service.GeoLocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.empik.couponix.common.utils.CouponUtils.normalizeToUpperCase;

@Service
@RequiredArgsConstructor
public class UseCouponService {

    private static final String USED_STATUS = "USED";

    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;
    private final GeoLocationService geoLocationService;
    private final TimeProvider timeProvider;

    @Transactional
    public UseCouponResponse useCoupon(UseCouponRequest request, String ipAddress) {
        String couponCode = normalizeToUpperCase(request.code());
        String userId = request.userId();
        String countryCode = normalizeToUpperCase(geoLocationService.resolveCountryCode(ipAddress));
        CouponEntity coupon = couponRepository.findByCodeForUpdate(couponCode)
                .orElseThrow(() -> new CouponNotFoundException(couponCode));

        validateCouponUsage(coupon, couponCode, userId, countryCode);
        saveCouponUsage(coupon, couponCode, userId, ipAddress);
        coupon.setCurrentUsages(coupon.getCurrentUsages() + 1);
        return new UseCouponResponse(coupon.getCode(), userId, USED_STATUS);
    }

    private void validateCouponUsage(
            CouponEntity coupon,
            String couponCode,
            String userId,
            String countryCode
    ) {
        if (!coupon.getCountryCode().equals(countryCode)) {
            throw new CouponCountryNotAllowedException(couponCode, countryCode);
        }

        if (couponUsageRepository.existsByCoupon_IdAndUserId(coupon.getId(), userId)) {
            throw new CouponAlreadyUsedByUserException(couponCode, userId);
        }

        if (coupon.getCurrentUsages() >= coupon.getMaxUsages()) {
            throw new CouponUsageLimitExceededException(couponCode);
        }
    }

    private void saveCouponUsage(
            CouponEntity coupon,
            String couponCode,
            String userId,
            String ipAddress
    ) {
        CouponUsageEntity usage = CouponUsageEntity.builder()
                .coupon(coupon)
                .userId(userId)
                .usedAt(timeProvider.now())
                .ipAddress(ipAddress)
                .build();

        try {
            couponUsageRepository.save(usage);
        } catch (DataIntegrityViolationException ex) {
            throw new CouponAlreadyUsedByUserException(couponCode, userId);
        }
    }
}