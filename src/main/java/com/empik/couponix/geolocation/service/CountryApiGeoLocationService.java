package com.empik.couponix.geolocation.service;

import com.empik.couponix.geolocation.dto.CountryApiResponse;
import com.empik.couponix.geolocation.exception.GeoLocationResolutionException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import static com.empik.couponix.common.utils.CouponUtils.normalizeToUpperCase;

@Service
@RequiredArgsConstructor
public class CountryApiGeoLocationService implements GeoLocationService {

    private static final String DEFAULT_LOCAL_COUNTRY_CODE = "PL";
    private static final String LOCALHOST = "127.0.0.1";
    private static final String LOCALHOST_IPV6 = "::1";

    private final RestClient countryApiRestClient;

    @Override
    public String resolveCountryCode(String ipAddress) {
        if (isLocalAddress(ipAddress)) {
            return DEFAULT_LOCAL_COUNTRY_CODE;
        }
        try {
            CountryApiResponse response = countryApiRestClient.get()
                    .uri("/{ip}", ipAddress)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, clientResponse) -> {
                        throw new GeoLocationResolutionException(
                                "Country API returned error status: " + clientResponse.getStatusCode()
                        );
                    })
                    .body(CountryApiResponse.class);

            return extractCountryCode(response);
        } catch (RestClientException ex) {
            throw new GeoLocationResolutionException("Failed to resolve country code for IP: " + ipAddress, ex);
        }
    }

    private boolean isLocalAddress(String ipAddress) {
        return StringUtils.isBlank(ipAddress)
                || LOCALHOST.equals(ipAddress)
                || LOCALHOST_IPV6.equals(ipAddress);
    }

    private String extractCountryCode(CountryApiResponse response) {
        if (response == null || response.country() == null || response.country().isBlank()) {
            throw new GeoLocationResolutionException("Country API returned empty country code");
        }

        return normalizeToUpperCase(response.country());
    }
}