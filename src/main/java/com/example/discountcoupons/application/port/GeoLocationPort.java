package com.example.discountcoupons.application.port;

import com.example.discountcoupons.domain.model.CountryCode;

public interface GeoLocationPort {
    CountryCode lookupCountry(String ip);
}
