package com.empik.couponix;

import com.empik.couponix.geolocation.config.CountryApiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(CountryApiProperties.class)
public class CouponixApplication {

    public static void main(String[] args) {
        SpringApplication.run(CouponixApplication.class, args);
    }

}
