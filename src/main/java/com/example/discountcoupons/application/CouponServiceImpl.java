package com.example.discountcoupons.application;

import com.example.discountcoupons.application.port.GeoLocationPort;
import com.example.discountcoupons.application.strategy.CouponInitializer;
import com.example.discountcoupons.application.strategy.CouponRedemptionStrategy;
import com.example.discountcoupons.domain.exception.CouponAlreadyUsedException;
import com.example.discountcoupons.domain.exception.CouponCodeDuplicateException;
import com.example.discountcoupons.domain.exception.CouponExhaustedException;
import com.example.discountcoupons.domain.exception.CouponNotFoundException;
import com.example.discountcoupons.domain.exception.CountryNotAllowedException;
import com.example.discountcoupons.domain.model.Coupon;
import com.example.discountcoupons.domain.model.CountryCode;
import com.example.discountcoupons.domain.model.CouponUsage;
import com.example.discountcoupons.infrastructure.persistence.CouponRepository;
import com.example.discountcoupons.infrastructure.persistence.CouponUsageRepository;
import java.util.Locale;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final CouponUsageRepository usageRepository;
    private final GeoLocationPort geoLocationPort;
    private final CouponRedemptionStrategy strategy;
    private final Optional<CouponInitializer> initializer;

    public CouponServiceImpl(CouponRepository couponRepository,
                             CouponUsageRepository usageRepository,
                             GeoLocationPort geoLocationPort,
                             CouponRedemptionStrategy strategy,
                             Optional<CouponInitializer> initializer) {
        this.couponRepository = couponRepository;
        this.usageRepository = usageRepository;
        this.geoLocationPort = geoLocationPort;
        this.strategy = strategy;
        this.initializer = initializer;
    }

    @Override
    @Transactional
    public CouponView createCoupon(String code, String countryCode, int maxUses) {
        String normalizedCode = code.toUpperCase(Locale.ROOT);
        CountryCode country = CountryCode.of(countryCode);
        if (couponRepository.existsByCode(normalizedCode)) {
            throw new CouponCodeDuplicateException(normalizedCode);
        }
        Coupon coupon = new Coupon(normalizedCode, country, maxUses);
        Coupon saved;
        try {
            saved = couponRepository.saveAndFlush(coupon);
        } catch (DataIntegrityViolationException ex) {
            throw new CouponCodeDuplicateException(normalizedCode);
        }
        initializer.ifPresent(i -> i.initialize(saved));
        return new CouponView(
                saved.getCode(),
                saved.getCountryCode().value(),
                saved.getMaxUses(),
                strategy.computeRemainingUses(saved),
                saved.getCreatedAt()
        );
    }

    @Override
    @Transactional
    public RedemptionResult redeem(String code, String userId, String ip) {
        String normalizedCode = code.toUpperCase(Locale.ROOT);
        Coupon coupon = couponRepository.findByCode(normalizedCode)
                .orElseThrow(() -> new CouponNotFoundException(normalizedCode));

        CountryCode userCountry = geoLocationPort.lookupCountry(ip);
        if (!coupon.getCountryCode().equals(userCountry)) {
            throw new CountryNotAllowedException(coupon.getCountryCode(), userCountry);
        }

        try {
            usageRepository.saveAndFlush(
                    new CouponUsage(coupon.getId(), userId, ip, userCountry));
        } catch (DataIntegrityViolationException ex) {
            throw new CouponAlreadyUsedException(normalizedCode, userId);
        }

        if (!strategy.tryRedeem(coupon, userId)) {
            throw new CouponExhaustedException(normalizedCode);
        }

        int remaining = strategy.computeRemainingUses(coupon);
        return new RedemptionResult(coupon.getCode(), remaining);
    }
}
