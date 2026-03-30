package com.empik.couponix.coupon.controller;

import com.empik.couponix.common.web.ClientIpResolver;
import com.empik.couponix.coupon.dto.request.CreateCouponRequest;
import com.empik.couponix.coupon.dto.request.UseCouponRequest;
import com.empik.couponix.coupon.dto.response.CreateCouponResponse;
import com.empik.couponix.coupon.dto.response.UseCouponResponse;
import com.empik.couponix.coupon.service.CreateCouponService;
import com.empik.couponix.coupon.service.UseCouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
@Tag(name = "Coupons", description = "API for creating and using discount coupons")
public class CouponController {

    private final CreateCouponService createCouponService;
    private final UseCouponService useCouponService;
    private final ClientIpResolver clientIpResolver;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new coupon")
    public CreateCouponResponse createCoupon(@Valid @RequestBody CreateCouponRequest request) {
        return createCouponService.createCoupon(request);
    }

    @PostMapping("/use")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Use a coupon")
    public UseCouponResponse useCoupon(
            @Valid @RequestBody UseCouponRequest request,
            HttpServletRequest httpServletRequest
    ) {
        String ipAddress = clientIpResolver.resolve(httpServletRequest);
        return useCouponService.useCoupon(request, ipAddress);
    }
}