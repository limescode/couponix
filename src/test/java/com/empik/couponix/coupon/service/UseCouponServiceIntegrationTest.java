package com.empik.couponix.coupon.service;

import com.empik.couponix.TestcontainersConfiguration;
import com.empik.couponix.common.time.TimeProvider;
import com.empik.couponix.coupon.dto.request.UseCouponRequest;
import com.empik.couponix.coupon.dto.response.UseCouponResponse;
import com.empik.couponix.coupon.entity.CouponEntity;
import com.empik.couponix.coupon.exception.CouponUsageLimitExceededException;
import com.empik.couponix.coupon.repository.CouponRepository;
import com.empik.couponix.coupon.repository.CouponUsageRepository;
import com.empik.couponix.geolocation.service.GeoLocationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class UseCouponServiceIntegrationTest {

    private static final String COUPON_CODE = "WAKACJE";
    private static final String IP_ADDRESS = "83.12.45.67";

    @Autowired
    private UseCouponService useCouponService;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponUsageRepository couponUsageRepository;

    @MockitoBean
    private GeoLocationService geoLocationService;

    @MockitoBean
    private TimeProvider timeProvider;

    @BeforeEach
    void setUp() {
        couponUsageRepository.deleteAll();
        couponRepository.deleteAll();

        when(geoLocationService.resolveCountryCode(IP_ADDRESS)).thenReturn("PL");
        when(timeProvider.now()).thenReturn(Instant.parse("2026-03-30T12:00:00Z"));
    }

    @AfterEach
    void tearDown() {
        couponUsageRepository.deleteAll();
        couponRepository.deleteAll();
    }

    @Test
    void shouldAllowOnlyOneSuccessfulCouponUsageWhenRequestsAreConcurrent() throws Exception {
        // given
        CouponEntity coupon = couponRepository.save(CouponEntity.builder()
                .code(COUPON_CODE)
                .createdAt(Instant.parse("2026-03-30T10:00:00Z"))
                .maxUsages(1)
                .currentUsages(0)
                .countryCode("PL")
                .build());

        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        Future<Object> firstResult = executor.submit(createUseCouponTask("jan", readyLatch, startLatch));
        Future<Object> secondResult = executor.submit(createUseCouponTask("anna", readyLatch, startLatch));

        readyLatch.await();
        startLatch.countDown();

        Object result1 = firstResult.get();
        Object result2 = secondResult.get();

        executor.shutdown();

        // then
        List<Object> results = new ArrayList<>();
        results.add(result1);
        results.add(result2);

        long successCount = results.stream()
                .filter(UseCouponResponse.class::isInstance)
                .count();

        long limitExceededCount = results.stream()
                .filter(CouponUsageLimitExceededException.class::isInstance)
                .count();

        assertThat(successCount).isEqualTo(1);
        assertThat(limitExceededCount).isEqualTo(1);

        CouponEntity savedCoupon = couponRepository.findById(coupon.getId()).orElseThrow();
        assertThat(savedCoupon.getCurrentUsages()).isEqualTo(1);
        assertThat(couponUsageRepository.count()).isEqualTo(1);
    }

    private Callable<Object> createUseCouponTask(
            String userId,
            CountDownLatch readyLatch,
            CountDownLatch startLatch
    ) {
        return () -> {
            readyLatch.countDown();
            startLatch.await();

            try {
                return useCouponService.useCoupon(new UseCouponRequest(COUPON_CODE, userId), IP_ADDRESS);
            } catch (Exception ex) {
                return ex;
            }
        };
    }
}