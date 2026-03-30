package com.empik.couponix.geolocation.config;

import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class GeoLocationClientConfig {

    @Bean
    public RestClient countryApiRestClient(CountryApiProperties properties) {
        var settings = ClientHttpRequestFactorySettings.defaults()
                .withConnectTimeout(Duration.ofMillis(properties.connectTimeoutMs()))
                .withReadTimeout(Duration.ofMillis(properties.readTimeoutMs()));

        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(ClientHttpRequestFactoryBuilder.simple().build(settings))
                .build();
    }
}