package com.example.discountcoupons.domain.exception;

import com.example.discountcoupons.domain.model.CountryCode;

public class CountryNotAllowedException extends RuntimeException {
    private final CountryCode allowedCountry;
    private final CountryCode actualCountry;

    public CountryNotAllowedException(CountryCode allowedCountry, CountryCode actualCountry) {
        super("Country not allowed: allowed=" + allowedCountry.value()
                + ", actual=" + actualCountry.value());
        this.allowedCountry = allowedCountry;
        this.actualCountry = actualCountry;
    }

    public CountryCode getAllowedCountry() {
        return allowedCountry;
    }

    public CountryCode getActualCountry() {
        return actualCountry;
    }
}
