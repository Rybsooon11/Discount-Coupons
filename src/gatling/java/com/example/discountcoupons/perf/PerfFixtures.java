package com.example.discountcoupons.perf;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import java.util.UUID;

final class PerfFixtures {

    static final String BASE_URL = System.getProperty("perf.baseUrl", "http://localhost:8080");
    static final int USERS = Integer.getInteger("perf.users", 1000);
    static final int DURATION_SECONDS = Integer.getInteger("perf.duration", 60);

    private PerfFixtures() {
    }

    static HttpProtocolBuilder httpProtocol() {
        return http
                .baseUrl(BASE_URL)
                .acceptHeader("application/json")
                .contentTypeHeader("application/json")
                .shareConnections();
    }

    static ScenarioBuilder createCoupon(String code, int maxUses) {
        return scenario("create-coupon")
                .exec(http("POST /coupons")
                        .post("/api/v1/coupons")
                        .body(StringBody("""
                                {"code":"%s","maxUses":%d,"countryCode":"PL"}
                                """.formatted(code, maxUses)))
                        .check(status().in(201, 409)));
    }

    static ChainBuilder redeem(String code, boolean acceptExhausted) {
        ChainBuilder request = exec(http("POST /coupons/redeem")
                .post("/api/v1/coupons/redeem")
                .body(StringBody("""
                        {"code":"#{code}","userId":"#{userId}"}
                        """))
                .check(acceptExhausted ? status().in(200, 409) : status().is(200)));

        return exec(session -> session
                .set("userId", UUID.randomUUID().toString())
                .set("code", code))
                .exec(request);
    }

    static String uniqueCode(String prefix) {
        return prefix + "_" + System.currentTimeMillis();
    }
}
