package com.empik.couponix.coupon.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import static com.empik.couponix.common.validation.ValidationMessages.COUNTRY_CODE_INVALID;
import static com.empik.couponix.common.validation.ValidationMessages.COUPON_CODE_INVALID;
import static com.empik.couponix.common.validation.ValidationPatterns.COUNTRY_CODE;
import static com.empik.couponix.common.validation.ValidationPatterns.COUPON_CODE;

@Schema(description = "Request for creating a new coupon")
public record CreateCouponRequest(

        @Schema(
                description = "Unique coupon code",
                example = "WAKACJE"
        )
        @NotBlank
        @Size(max = 100)
        @Pattern(regexp = COUPON_CODE, message = COUPON_CODE_INVALID)
        String code,

        @Schema(
                description = "Maximum number of coupon usages",
                example = "5"
        )
        @NotNull
        @Positive
        Integer maxUsages,

        @Schema(
                description = "Country code in ISO 3166-1 alpha-2 format",
                example = "PL"
        )
        @NotBlank
        @Size(min = 2, max = 2)
        @Pattern(regexp = COUNTRY_CODE, message = COUNTRY_CODE_INVALID)
        String countryCode
) {
}