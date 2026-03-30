package com.empik.couponix.coupon.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import static com.empik.couponix.common.validation.ValidationMessages.COUPON_CODE_INVALID;
import static com.empik.couponix.common.validation.ValidationPatterns.COUPON_CODE;

@Schema(description = "Request for using a coupon")
public record UseCouponRequest(

        @Schema(
                description = "Coupon code",
                example = "WAKACJE"
        )
        @NotBlank
        @Size(max = 100)
        @Pattern(regexp = COUPON_CODE, message = COUPON_CODE_INVALID)
        String code,

        @Schema(
                description = "User identifier",
                example = "janina"
        )
        @NotBlank
        @Size(max = 100)
        String userId
) {
}