package com.example.discountcoupons.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.discountcoupons.application.port.GeoLocationPort;
import com.example.discountcoupons.application.strategy.CouponInitializer;
import com.example.discountcoupons.application.strategy.CouponRedemptionStrategy;
import com.example.discountcoupons.domain.exception.CountryNotAllowedException;
import com.example.discountcoupons.domain.exception.CouponAlreadyUsedException;
import com.example.discountcoupons.domain.exception.CouponExhaustedException;
import com.example.discountcoupons.domain.exception.CouponNotFoundException;
import com.example.discountcoupons.domain.model.Coupon;
import com.example.discountcoupons.domain.model.CountryCode;
import com.example.discountcoupons.domain.model.CouponUsage;
import com.example.discountcoupons.infrastructure.persistence.CouponRepository;
import com.example.discountcoupons.infrastructure.persistence.CouponUsageRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CouponServiceUnitTest {

    @Mock CouponRepository couponRepository;
    @Mock CouponUsageRepository usageRepository;
    @Mock GeoLocationPort geoLocationPort;
    @Mock CouponRedemptionStrategy strategy;
    @Mock CouponInitializer initializer;

    CouponServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CouponServiceImpl(
                couponRepository, usageRepository, geoLocationPort, strategy, Optional.of(initializer));
    }

    @Test
    void redeem_not_found_throws() {
        given(couponRepository.findByCode("WIOSNA")).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.redeem("wiosna", "u1", "1.2.3.4"))
                .isInstanceOf(CouponNotFoundException.class);

        verify(usageRepository, never()).saveAndFlush(any());
    }

    @Test
    void redeem_country_mismatch_throws() {
        Coupon coupon = couponFor("WIOSNA", "PL", 10);
        given(couponRepository.findByCode("WIOSNA")).willReturn(Optional.of(coupon));
        given(geoLocationPort.lookupCountry("1.2.3.4")).willReturn(CountryCode.of("DE"));

        assertThatThrownBy(() -> service.redeem("WIOSNA", "u1", "1.2.3.4"))
                .isInstanceOf(CountryNotAllowedException.class);

        verify(usageRepository, never()).saveAndFlush(any());
    }

    @Test
    void redeem_already_used_throws() {
        Coupon coupon = couponFor("WIOSNA", "PL", 10);
        given(couponRepository.findByCode("WIOSNA")).willReturn(Optional.of(coupon));
        given(geoLocationPort.lookupCountry(anyString())).willReturn(CountryCode.of("PL"));
        willThrow(new DataIntegrityViolationException("unique"))
                .given(usageRepository).saveAndFlush(any(CouponUsage.class));

        assertThatThrownBy(() -> service.redeem("WIOSNA", "u1", "1.2.3.4"))
                .isInstanceOf(CouponAlreadyUsedException.class);
    }

    @Test
    void redeem_exhausted_throws() {
        Coupon coupon = couponFor("WIOSNA", "PL", 10);
        given(couponRepository.findByCode("WIOSNA")).willReturn(Optional.of(coupon));
        given(geoLocationPort.lookupCountry(anyString())).willReturn(CountryCode.of("PL"));
        given(strategy.tryRedeem(coupon, "u1")).willReturn(false);

        assertThatThrownBy(() -> service.redeem("WIOSNA", "u1", "1.2.3.4"))
                .isInstanceOf(CouponExhaustedException.class);
    }

    @Test
    void redeem_success_returns_remaining() {
        Coupon coupon = couponFor("WIOSNA", "PL", 10);
        given(couponRepository.findByCode("WIOSNA")).willReturn(Optional.of(coupon));
        given(geoLocationPort.lookupCountry(anyString())).willReturn(CountryCode.of("PL"));
        given(strategy.tryRedeem(coupon, "u1")).willReturn(true);
        given(strategy.computeRemainingUses(coupon)).willReturn(9);

        var result = service.redeem("WIOSNA", "u1", "1.2.3.4");

        assertThat(result.code()).isEqualTo("WIOSNA");
        assertThat(result.remainingUses()).isEqualTo(9);
    }

    private static Coupon couponFor(String code, String country, int maxUses) {
        Coupon coupon = new Coupon(code, CountryCode.of(country), maxUses);
        ReflectionTestUtils.setField(coupon, "id", 1L);
        return coupon;
    }
}
