package com.empik.couponix.common.web;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ClientIpResolverTest {

    private static final String X_FORWARDED_FOR = "X-Forwarded-For";

    private final ClientIpResolver clientIpResolver = new ClientIpResolver();

    @Test
    void shouldReturnFirstIpAddressFromXForwardedForHeader() {
        // given
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(X_FORWARDED_FOR)).thenReturn("83.12.45.67, 10.0.0.1, 192.168.1.10");

        // when
        String result = clientIpResolver.resolve(request);

        // then
        assertThat(result).isEqualTo("83.12.45.67");
    }

    @Test
    void shouldReturnRemoteAddressWhenXForwardedForHeaderIsBlank() {
        // given
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(X_FORWARDED_FOR)).thenReturn("   ");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        // when
        String result = clientIpResolver.resolve(request);

        // then
        assertThat(result).isEqualTo("127.0.0.1");
    }

    @Test
    void shouldReturnRemoteAddressWhenXForwardedForHeaderIsMissing() {
        // given
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(X_FORWARDED_FOR)).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.0.15");

        // when
        String result = clientIpResolver.resolve(request);

        // then
        assertThat(result).isEqualTo("192.168.0.15");
    }
}