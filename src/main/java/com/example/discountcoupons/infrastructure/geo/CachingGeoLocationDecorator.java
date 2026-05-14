package com.example.discountcoupons.infrastructure.geo;

import com.example.discountcoupons.application.port.GeoLocationPort;
import com.example.discountcoupons.domain.model.CountryCode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Primary
@Profile("!perf")
public class CachingGeoLocationDecorator implements GeoLocationPort {

    private final GeoLocationPort delegate;

    public CachingGeoLocationDecorator(
            @Qualifier("ipApiGeoLocationAdapter") GeoLocationPort delegate) {
        this.delegate = delegate;
    }

    @Override
    @Cacheable(cacheNames = "geoCountryByIp", unless = "#result == null")
    public CountryCode lookupCountry(String ip) {
        return delegate.lookupCountry(ip);
    }
}
