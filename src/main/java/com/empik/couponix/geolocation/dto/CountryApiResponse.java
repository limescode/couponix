package com.empik.couponix.geolocation.dto;

public record CountryApiResponse(
        String ip,
        String country
) {
}