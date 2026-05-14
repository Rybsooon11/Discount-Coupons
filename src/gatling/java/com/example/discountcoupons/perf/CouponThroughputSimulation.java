package com.example.discountcoupons.perf;

import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;
import static io.gatling.javaapi.core.CoreDsl.constantConcurrentUsers;
import static io.gatling.javaapi.core.CoreDsl.global;
import static io.gatling.javaapi.core.CoreDsl.scenario;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import java.time.Duration;

public class CouponThroughputSimulation extends Simulation {

    private static final int MAX_USES = Integer.getInteger("perf.maxUses", 10_000_000);
    private static final String COUPON_CODE = PerfFixtures.uniqueCode("THRU");

    private final ScenarioBuilder loopingRedeem = scenario("redeem-throughput")
            .during(Duration.ofSeconds(PerfFixtures.DURATION_SECONDS))
            .on(PerfFixtures.redeem(COUPON_CODE, false));

    {
        setUp(
                PerfFixtures.createCoupon(COUPON_CODE, MAX_USES).injectOpen(atOnceUsers(1))
                        .andThen(
                                loopingRedeem.injectClosed(
                                        constantConcurrentUsers(PerfFixtures.USERS)
                                                .during(Duration.ofSeconds(PerfFixtures.DURATION_SECONDS))
                                )
                        )
        )
        .protocols(PerfFixtures.httpProtocol())
        .assertions(
                global().failedRequests().percent().lte(1.0)
        );
    }
}
