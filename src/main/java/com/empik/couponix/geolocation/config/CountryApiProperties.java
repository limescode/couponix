package com.empik.couponix.geolocation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "geolocation.country-api")
public record CountryApiProperties(
        String baseUrl,
        int connectTimeoutMs,
        int readTimeoutMs
) {
}