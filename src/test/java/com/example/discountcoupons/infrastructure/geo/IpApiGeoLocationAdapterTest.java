package com.example.discountcoupons.infrastructure.geo;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.discountcoupons.domain.exception.GeoLocationException;
import com.example.discountcoupons.domain.model.CountryCode;
import com.example.discountcoupons.infrastructure.config.WebClientConfig;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IpApiGeoLocationAdapterTest {

    private static WireMockServer wireMock;
    private IpApiGeoLocationAdapter adapter;

    @BeforeAll
    static void startWireMock() {
        wireMock = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMock.start();
    }

    @AfterAll
    static void stopWireMock() {
        wireMock.stop();
    }

    @BeforeEach
    void setUp() {
        wireMock.resetAll();
        var client = new WebClientConfig().geoWebClient("http://localhost:" + wireMock.port(), 2000);
        adapter = new IpApiGeoLocationAdapter(client);
    }

    @Test
    void returns_country_on_success() {
        wireMock.stubFor(get(urlPathMatching("/json/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"status":"success","countryCode":"PL"}
                                """)));

        assertThat(adapter.lookupCountry("91.108.0.1")).isEqualTo(CountryCode.of("PL"));
    }

    @Test
    void throws_on_fail_status() {
        wireMock.stubFor(get(urlPathMatching("/json/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"status":"fail","message":"private range"}
                                """)));

        assertThatThrownBy(() -> adapter.lookupCountry("10.0.0.1"))
                .isInstanceOf(GeoLocationException.class);
    }

    @Test
    void throws_on_5xx() {
        wireMock.stubFor(get(urlPathMatching("/json/.*"))
                .willReturn(aResponse().withStatus(500)));

        assertThatThrownBy(() -> adapter.lookupCountry("1.2.3.4"))
                .isInstanceOf(GeoLocationException.class);
    }
}
