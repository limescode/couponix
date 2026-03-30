package com.empik.couponix.coupon.service;

import com.empik.couponix.common.time.TimeProvider;
import com.empik.couponix.coupon.dto.request.UseCouponRequest;
import com.empik.couponix.coupon.dto.response.UseCouponResponse;
import com.empik.couponix.coupon.entity.CouponEntity;
import com.empik.couponix.coupon.entity.CouponUsageEntity;
import com.empik.couponix.coupon.exception.CouponAlreadyUsedByUserException;
import com.empik.couponix.coupon.exception.CouponCountryNotAllowedException;
import com.empik.couponix.coupon.exception.CouponUsageLimitExceededException;
import com.empik.couponix.coupon.repository.CouponRepository;
import com.empik.couponix.coupon.repository.CouponUsageRepository;
import com.empik.couponix.geolocation.service.GeoLocationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UseCouponServiceTest {

    private static final String COUPON_CODE = "wakacje";
    private static final String NORMALIZED_COUPON_CODE = "WAKACJE";
    private static final String IP_ADDRESS = "10.20.30.40";
    private static final Instant USED_AT = Instant.parse("2026-03-30T12:00:00Z");
    private static final Instant CREATED_AT = Instant.parse("2026-03-29T10:00:00Z");

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponUsageRepository couponUsageRepository;

    @Mock
    private GeoLocationService geoLocationService;

    @Mock
    private TimeProvider timeProvider;

    @InjectMocks
    private UseCouponService useCouponService;

    @Captor
    private ArgumentCaptor<CouponUsageEntity> couponUsageCaptor;

    @Test
    void shouldUseCoupon() {
        // given
        UseCouponRequest request = useCouponRequest("jan");
        CouponEntity coupon = couponEntity(3, 1, "PL");

        when(couponRepository.findByCodeForUpdate(NORMALIZED_COUPON_CODE)).thenReturn(Optional.of(coupon));
        when(geoLocationService.resolveCountryCode(IP_ADDRESS)).thenReturn("pl");
        when(couponUsageRepository.existsByCoupon_IdAndUserId(coupon.getId(), "jan")).thenReturn(false);
        when(timeProvider.now()).thenReturn(USED_AT);

        // when
        UseCouponResponse result = useCouponService.useCoupon(request, IP_ADDRESS);

        // then
        assertThat(result).isEqualTo(new UseCouponResponse(NORMALIZED_COUPON_CODE, "jan", "USED"));
        assertThat(coupon.getCurrentUsages()).isEqualTo(2);

        verify(couponRepository).findByCodeForUpdate(NORMALIZED_COUPON_CODE);
        verify(geoLocationService).resolveCountryCode(IP_ADDRESS);
        verify(couponUsageRepository).existsByCoupon_IdAndUserId(coupon.getId(), "jan");
        verify(couponUsageRepository).save(couponUsageCaptor.capture());

        CouponUsageEntity savedUsage = couponUsageCaptor.getValue();
        assertThat(savedUsage.getCoupon()).isSameAs(coupon);
        assertThat(savedUsage.getUserId()).isEqualTo("jan");
        assertThat(savedUsage.getUsedAt()).isEqualTo(USED_AT);
        assertThat(savedUsage.getIpAddress()).isEqualTo(IP_ADDRESS);
    }

    @Test
    void shouldThrowCouponCountryNotAllowedExceptionWhenCountryDoesNotMatch() {
        // given
        UseCouponRequest request = useCouponRequest("jan");
        CouponEntity coupon = couponEntity(3, 1, "PL");

        when(couponRepository.findByCodeForUpdate(NORMALIZED_COUPON_CODE)).thenReturn(Optional.of(coupon));
        when(geoLocationService.resolveCountryCode(IP_ADDRESS)).thenReturn("de");

        // when & then
        assertThatThrownBy(() -> useCouponService.useCoupon(request, IP_ADDRESS))
                .isInstanceOf(CouponCountryNotAllowedException.class)
                .hasMessage("Coupon with code 'WAKACJE' cannot be used from country 'DE'");

        verify(couponUsageRepository, never()).save(any());
    }

    @Test
    void shouldThrowCouponAlreadyUsedByUserExceptionWhenUserAlreadyUsedCoupon() {
        // given
        UseCouponRequest request = useCouponRequest("janina");
        CouponEntity coupon = couponEntity(3, 1, "PL");

        when(couponRepository.findByCodeForUpdate(NORMALIZED_COUPON_CODE)).thenReturn(Optional.of(coupon));
        when(geoLocationService.resolveCountryCode(IP_ADDRESS)).thenReturn("PL");
        when(couponUsageRepository.existsByCoupon_IdAndUserId(coupon.getId(), "janina")).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> useCouponService.useCoupon(request, IP_ADDRESS))
                .isInstanceOf(CouponAlreadyUsedByUserException.class)
                .hasMessage("Coupon with code 'WAKACJE' has already been used by user 'janina'");

        verify(couponUsageRepository, never()).save(any());
    }

    @Test
    void shouldThrowCouponUsageLimitExceededExceptionWhenCouponReachedLimit() {
        // given
        UseCouponRequest request = useCouponRequest("jan");
        CouponEntity coupon = couponEntity(2, 2, "PL");

        when(couponRepository.findByCodeForUpdate(NORMALIZED_COUPON_CODE)).thenReturn(Optional.of(coupon));
        when(geoLocationService.resolveCountryCode(IP_ADDRESS)).thenReturn("PL");
        when(couponUsageRepository.existsByCoupon_IdAndUserId(coupon.getId(), "jan")).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> useCouponService.useCoupon(request, IP_ADDRESS))
                .isInstanceOf(CouponUsageLimitExceededException.class)
                .hasMessage("Coupon with code 'WAKACJE' has reached maximum number of usages");

        verify(couponUsageRepository, never()).save(any());
    }

    @Test
    void shouldThrowCouponAlreadyUsedByUserExceptionWhenSavingUsageFailsBecauseOfDuplicate() {
        // given
        UseCouponRequest request = useCouponRequest("jan");

        CouponEntity coupon = couponEntity(3, 1, "PL");

        when(couponRepository.findByCodeForUpdate(NORMALIZED_COUPON_CODE)).thenReturn(Optional.of(coupon));
        when(geoLocationService.resolveCountryCode(IP_ADDRESS)).thenReturn("PL");
        when(couponUsageRepository.existsByCoupon_IdAndUserId(coupon.getId(), "jan")).thenReturn(false);
        when(timeProvider.now()).thenReturn(USED_AT);
        when(couponUsageRepository.save(any(CouponUsageEntity.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        // when & then
        assertThatThrownBy(() -> useCouponService.useCoupon(request, IP_ADDRESS))
                .isInstanceOf(CouponAlreadyUsedByUserException.class)
                .hasMessage("Coupon with code 'WAKACJE' has already been used by user 'jan'");

        assertThat(coupon.getCurrentUsages()).isEqualTo(1);
    }

    private UseCouponRequest useCouponRequest(String userId) {
        return new UseCouponRequest(COUPON_CODE, userId);
    }

    private CouponEntity couponEntity(int maxUsages, int currentUsages, String countryCode) {
        return CouponEntity.builder()
                .id(UUID.randomUUID())
                .code(NORMALIZED_COUPON_CODE)
                .createdAt(CREATED_AT)
                .maxUsages(maxUsages)
                .currentUsages(currentUsages)
                .countryCode(countryCode)
                .build();
    }
}