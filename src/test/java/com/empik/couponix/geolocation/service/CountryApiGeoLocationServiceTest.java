package com.empik.couponix.geolocation.service;

import com.empik.couponix.geolocation.dto.CountryApiResponse;
import com.empik.couponix.geolocation.exception.GeoLocationResolutionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CountryApiGeoLocationServiceTest {

    private static final String IP_ADDRESS = "83.12.45.67";

    @Mock
    private RestClient countryApiRestClient;

    @SuppressWarnings("rawtypes")
    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @SuppressWarnings("rawtypes")
    @Mock
    private RestClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @Test
    void shouldReturnDefaultCountryCodeWhenIpAddressIsBlank() {
        // given
        CountryApiGeoLocationService service = new CountryApiGeoLocationService(countryApiRestClient);

        // when
        String result = service.resolveCountryCode("  ");

        // then
        assertThat(result).isEqualTo("PL");
        verify(countryApiRestClient, never()).get();
    }

    @Test
    void shouldReturnDefaultCountryCodeWhenIpAddressIsLocalhost() {
        // given
        CountryApiGeoLocationService service = new CountryApiGeoLocationService(countryApiRestClient);

        // when
        String result = service.resolveCountryCode("127.0.0.1");

        // then
        assertThat(result).isEqualTo("PL");
        verify(countryApiRestClient, never()).get();
    }

    @Test
    void shouldReturnDefaultCountryCodeWhenIpAddressIsLocalhostIpv6() {
        // given
        CountryApiGeoLocationService service = new CountryApiGeoLocationService(countryApiRestClient);

        // when
        String result = service.resolveCountryCode("::1");

        // then
        assertThat(result).isEqualTo("PL");
        verify(countryApiRestClient, never()).get();
    }

    @Test
    void shouldReturnNormalizedCountryCodeWhenCountryApiReturnsCountry() {
        // given
        CountryApiGeoLocationService service = new CountryApiGeoLocationService(countryApiRestClient);

        when(countryApiRestClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/{ip}", IP_ADDRESS)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.body(CountryApiResponse.class)).thenReturn(new CountryApiResponse(IP_ADDRESS, "pl"));

        // when
        String result = service.resolveCountryCode(IP_ADDRESS);

        // then
        assertThat(result).isEqualTo("PL");
        verify(requestHeadersUriSpec).uri("/{ip}", IP_ADDRESS);
        verify(responseSpec).body(CountryApiResponse.class);
    }

    @Test
    void shouldThrowExceptionWhenCountryApiReturnsNullResponse() {
        // given
        CountryApiGeoLocationService service = new CountryApiGeoLocationService(countryApiRestClient);

        when(countryApiRestClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/{ip}", IP_ADDRESS)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.body(CountryApiResponse.class)).thenReturn(null);

        // when / then
        assertThatThrownBy(() -> service.resolveCountryCode(IP_ADDRESS))
                .isInstanceOf(GeoLocationResolutionException.class)
                .hasMessage("Country API returned empty country code");
    }

    @Test
    void shouldThrowExceptionWhenCountryApiReturnsBlankCountry() {
        // given
        CountryApiGeoLocationService service = new CountryApiGeoLocationService(countryApiRestClient);

        when(countryApiRestClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/{ip}", IP_ADDRESS)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.body(CountryApiResponse.class)).thenReturn(new CountryApiResponse(IP_ADDRESS, " "));

        // when / then
        assertThatThrownBy(() -> service.resolveCountryCode(IP_ADDRESS))
                .isInstanceOf(GeoLocationResolutionException.class)
                .hasMessage("Country API returned empty country code");
    }

    @Test
    void shouldThrowExceptionWhenRestClientThrowsException() {
        // given
        CountryApiGeoLocationService service = new CountryApiGeoLocationService(countryApiRestClient);

        when(countryApiRestClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/{ip}", IP_ADDRESS))
                .thenThrow(new RestClientException("Connection timeout"));

        // when / then
        assertThatThrownBy(() -> service.resolveCountryCode(IP_ADDRESS))
                .isInstanceOf(GeoLocationResolutionException.class)
                .hasMessage("Failed to resolve country code for IP: " + IP_ADDRESS)
                .hasCauseInstanceOf(RestClientException.class);
    }
}