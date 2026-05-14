package com.example.discountcoupons.infrastructure.geo;

import com.example.discountcoupons.application.port.GeoLocationPort;
import com.example.discountcoupons.domain.model.CountryCode;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Primary
@Profile("perf")
public class StubGeoLocationPort implements GeoLocationPort {

    private static final CountryCode FIXED = CountryCode.of("PL");

    @Override
    public CountryCode lookupCountry(String ip) {
        return FIXED;
    }
}
