package com.example.discountcoupons.domain.exception;

public class GeoLocationException extends RuntimeException {
    public GeoLocationException(String ip, Throwable cause) {
        super("Geolocation unavailable for ip=" + ip, cause);
    }

    public GeoLocationException(String message) {
        super(message);
    }
}
