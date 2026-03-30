package com.empik.couponix.geolocation.exception;

public class GeoLocationResolutionException extends RuntimeException {

    public GeoLocationResolutionException(String message) {
        super(message);
    }

    public GeoLocationResolutionException(String message, Throwable cause) {
        super(message, cause);
    }
}