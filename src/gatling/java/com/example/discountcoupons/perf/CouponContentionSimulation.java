package com.example.discountcoupons.perf;

import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;
import static io.gatling.javaapi.core.CoreDsl.constantConcurrentUsers;
import static io.gatling.javaapi.core.CoreDsl.scenario;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import java.time.Duration;

public class CouponContentionSimulation extends Simulation {

    private static final int MAX_USES = Integer.getInteger("perf.maxUses", 10_000);
    private static final String COUPON_CODE = PerfFixtures.uniqueCode("CONT");

    private final ScenarioBuilder loopingRedeem = scenario("redeem-contention")
            .during(Duration.ofSeconds(PerfFixtures.DURATION_SECONDS))
            .on(PerfFixtures.redeem(COUPON_CODE, true));

    {
        setUp(
                PerfFixtures.createCoupon(COUPON_CODE, MAX_USES).injectOpen(atOnceUsers(1))
                        .andThen(
                                loopingRedeem.injectClosed(
                                        constantConcurrentUsers(PerfFixtures.USERS)
                                                .during(Duration.ofSeconds(PerfFixtures.DURATION_SECONDS))
                                )
                        )
        ).protocols(PerfFixtures.httpProtocol());
    }
}
