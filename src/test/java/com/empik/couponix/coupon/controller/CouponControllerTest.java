package com.empik.couponix.coupon.controller;

import com.empik.couponix.common.web.ClientIpResolver;
import com.empik.couponix.coupon.dto.request.CreateCouponRequest;
import com.empik.couponix.coupon.dto.request.UseCouponRequest;
import com.empik.couponix.coupon.dto.response.CreateCouponResponse;
import com.empik.couponix.coupon.dto.response.UseCouponResponse;
import com.empik.couponix.coupon.service.CreateCouponService;
import com.empik.couponix.coupon.service.UseCouponService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CouponController.class)
class CouponControllerTest {

    private static final String COUPONS_PATH = "/api/coupons";
    private static final String USE_COUPON_PATH = "/api/coupons/use";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateCouponService createCouponService;

    @MockitoBean
    private UseCouponService useCouponService;

    @MockitoBean
    private ClientIpResolver clientIpResolver;

    @Test
    void shouldCreateCoupon() throws Exception {
        // given
        CreateCouponRequest request = new CreateCouponRequest("wakacje", 5, "pl");
        CreateCouponResponse response = new CreateCouponResponse(
                UUID.randomUUID(),
                "WAKACJE",
                Instant.parse("2026-03-30T12:00:00Z"),
                5,
                0,
                "PL"
        );

        when(createCouponService.createCoupon(any(CreateCouponRequest.class))).thenReturn(response);

        // when & then
        mockMvc.perform(post(COUPONS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("WAKACJE"))
                .andExpect(jsonPath("$.maxUsages").value(5))
                .andExpect(jsonPath("$.currentUsages").value(0))
                .andExpect(jsonPath("$.countryCode").value("PL"));

        verify(createCouponService).createCoupon(any(CreateCouponRequest.class));
    }

    @Test
    void shouldReturnBadRequestWhenCreateCouponRequestIsInvalid() throws Exception {
        // given
        String requestBody = """
                {
                  "code": "",
                  "maxUsages": 0,
                  "countryCode": "POL"
                }
                """;

        // when & then
        mockMvc.perform(post(COUPONS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUseCoupon() throws Exception {
        // given
        UseCouponRequest request = new UseCouponRequest("wakacje", "jan");
        UseCouponResponse response = new UseCouponResponse("WAKACJE", "jan", "USED");

        when(clientIpResolver.resolve(any())).thenReturn("83.12.45.67");
        when(useCouponService.useCoupon(any(UseCouponRequest.class), same("83.12.45.67"))).thenReturn(response);

        // when & then
        mockMvc.perform(post(USE_COUPON_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("WAKACJE"))
                .andExpect(jsonPath("$.userId").value("jan"))
                .andExpect(jsonPath("$.status").value("USED"));

        verify(clientIpResolver).resolve(any());
        verify(useCouponService).useCoupon(any(UseCouponRequest.class), same("83.12.45.67"));
    }

    @Test
    void shouldReturnBadRequestWhenUseCouponRequestIsInvalid() throws Exception {
        // given
        String requestBody = """
                {
                  "code": "",
                  "userId": ""
                }
                """;

        // when & then
        mockMvc.perform(post(USE_COUPON_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }
}