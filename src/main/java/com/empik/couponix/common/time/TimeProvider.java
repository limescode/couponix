package com.empik.couponix.common.time;

import java.time.Instant;

public interface TimeProvider {
    Instant now();
}