package com.example.discountcoupons.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import com.example.discountcoupons.BaseIntegrationTest;
import com.example.discountcoupons.application.port.GeoLocationPort;
import com.example.discountcoupons.domain.exception.CouponExhaustedException;
import com.example.discountcoupons.domain.model.CountryCode;
import com.example.discountcoupons.infrastructure.persistence.CouponRepository;
import com.example.discountcoupons.infrastructure.persistence.CouponUsageRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class CouponServiceConcurrencyTest extends BaseIntegrationTest {

    private static final int THREADS = 100;
    private static final int MAX_USES = 10;

    @Autowired
    CouponService couponService;

    @Autowired
    CouponRepository couponRepository;

    @Autowired
    CouponUsageRepository usageRepository;

    @MockitoBean
    GeoLocationPort geoLocationPort;

    @Test
    void atomicUpdate_redeem_only_maxUses_successes() throws Exception {
        given(geoLocationPort.lookupCountry(anyString())).willReturn(CountryCode.of("PL"));
        couponRepository.deleteAll();

        CouponView coupon = couponService.createCoupon("CONC_ATOMIC", "PL", MAX_USES);

        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger successes = new AtomicInteger();
        AtomicInteger exhausted = new AtomicInteger();
        AtomicInteger other = new AtomicInteger();
        List<Future<?>> futures = new ArrayList<>(THREADS);

        for (int i = 0; i < THREADS; i++) {
            final String userId = "user-" + i;
            futures.add(executor.submit(() -> {
                start.await();
                try {
                    couponService.redeem(coupon.code(), userId, "203.0.113." + (userId.hashCode() & 0xFF));
                    successes.incrementAndGet();
                } catch (CouponExhaustedException ex) {
                    exhausted.incrementAndGet();
                } catch (Exception ex) {
                    other.incrementAndGet();
                }
                return null;
            }));
        }

        start.countDown();
        for (Future<?> f : futures) {
            f.get(60, TimeUnit.SECONDS);
        }
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        assertThat(successes.get()).isEqualTo(MAX_USES);
        assertThat(exhausted.get()).isEqualTo(THREADS - MAX_USES);
        assertThat(other.get()).isZero();
        Long couponId = couponRepository.findByCode(coupon.code()).orElseThrow().getId();
        assertThat(usageRepository.countByCouponId(couponId)).isEqualTo(MAX_USES);
        assertThat(couponRepository.findByCode(coupon.code()).orElseThrow().getCurrentUses())
                .isEqualTo(MAX_USES);
    }
}
