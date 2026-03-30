package com.empik.couponix;

import org.springframework.boot.SpringApplication;

public class TestCouponixApplication {

    public static void main(String[] args) {
        SpringApplication.from(CouponixApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
