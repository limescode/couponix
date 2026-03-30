package com.empik.couponix.coupon.service;

import com.empik.couponix.common.time.TimeProvider;
import com.empik.couponix.coupon.dto.request.CreateCouponRequest;
import com.empik.couponix.coupon.dto.response.CreateCouponResponse;
import com.empik.couponix.coupon.entity.CouponEntity;
import com.empik.couponix.coupon.exception.CouponAlreadyExistsException;
import com.empik.couponix.coupon.mapper.CouponMapper;
import com.empik.couponix.coupon.repository.CouponRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateCouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private TimeProvider timeProvider;

    @Mock
    private CouponMapper couponMapper;

    @InjectMocks
    private CreateCouponService createCouponService;

    @Test
    void shouldCreateCoupon() {
        // given
        CreateCouponRequest request = new CreateCouponRequest("wakacje", 5, "pl");
        Instant now = Instant.parse("2026-03-30T10:15:30Z");

        CouponEntity couponToSave = CouponEntity.builder()
                .code("WAKACJE")
                .createdAt(now)
                .maxUsages(5)
                .currentUsages(0)
                .countryCode("PL")
                .build();

        CouponEntity savedCoupon = CouponEntity.builder()
                .id(UUID.fromString(UUID.randomUUID().toString()))
                .code("WAKACJE")
                .createdAt(now)
                .maxUsages(5)
                .currentUsages(0)
                .countryCode("PL")
                .build();

        CreateCouponResponse expectedResponse = new CreateCouponResponse(
                savedCoupon.getId(),
                savedCoupon.getCode(),
                savedCoupon.getCreatedAt(),
                savedCoupon.getMaxUsages(),
                savedCoupon.getCurrentUsages(),
                savedCoupon.getCountryCode()
        );

        when(timeProvider.now()).thenReturn(now);
        when(couponMapper.toNewEntity(request, "WAKACJE", "PL", now)).thenReturn(couponToSave);
        when(couponRepository.save(couponToSave)).thenReturn(savedCoupon);
        when(couponMapper.toCreateCouponResponse(savedCoupon)).thenReturn(expectedResponse);

        // when
        CreateCouponResponse result = createCouponService.createCoupon(request);

        // then
        assertThat(result).isEqualTo(expectedResponse);
        verify(couponMapper).toNewEntity(request, "WAKACJE", "PL", now);
        verify(couponRepository).save(couponToSave);
        verify(couponMapper).toCreateCouponResponse(savedCoupon);
    }

    @Test
    void shouldThrowCouponAlreadyExistsExceptionWhenCouponCodeAlreadyExists() {
        // given
        CreateCouponRequest request = new CreateCouponRequest("wakacje", 5, "pl");
        Instant now = Instant.parse("2026-03-30T10:15:30Z");

        CouponEntity couponToSave = CouponEntity.builder()
                .code("WAKACJE")
                .createdAt(now)
                .maxUsages(5)
                .currentUsages(0)
                .countryCode("PL")
                .build();

        when(timeProvider.now()).thenReturn(now);
        when(couponMapper.toNewEntity(request, "WAKACJE", "PL", now)).thenReturn(couponToSave);
        when(couponRepository.save(couponToSave))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        // when & then
        assertThatThrownBy(() -> createCouponService.createCoupon(request))
                .isInstanceOf(CouponAlreadyExistsException.class)
                .hasMessage("Coupon with code 'WAKACJE' already exists");
    }
}