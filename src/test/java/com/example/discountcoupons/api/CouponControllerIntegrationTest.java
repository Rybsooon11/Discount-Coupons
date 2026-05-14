package com.example.discountcoupons.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.discountcoupons.BaseIntegrationTest;
import com.example.discountcoupons.application.port.GeoLocationPort;
import com.example.discountcoupons.domain.exception.GeoLocationException;
import com.example.discountcoupons.domain.model.CountryCode;
import com.example.discountcoupons.infrastructure.persistence.CouponRepository;
import com.example.discountcoupons.infrastructure.persistence.CouponUsageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class CouponControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired CouponRepository couponRepository;
    @Autowired CouponUsageRepository usageRepository;

    @MockitoBean GeoLocationPort geoLocationPort;

    @BeforeEach
    void cleanup() {
        usageRepository.deleteAll();
        couponRepository.deleteAll();
    }

    @Test
    void create_then_redeem_happy_path() throws Exception {
        mockMvc.perform(post("/api/v1/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"WIOSNA","maxUses":2,"countryCode":"PL"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/coupons/WIOSNA"))
                .andExpect(jsonPath("$.code").value("WIOSNA"))
                .andExpect(jsonPath("$.maxUses").value(2))
                .andExpect(jsonPath("$.remainingUses").value(2));

        given(geoLocationPort.lookupCountry(anyString())).willReturn(CountryCode.of("PL"));

        mockMvc.perform(post("/api/v1/coupons/redeem")
                        .header("X-Forwarded-For", "91.108.0.1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"wiosna","userId":"u1"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("WIOSNA"))
                .andExpect(jsonPath("$.remainingUses").value(1));

        assertThat(couponRepository.findByCode("WIOSNA").orElseThrow().getCurrentUses()).isEqualTo(1);
    }

    @Test
    void redeem_returns_404_when_unknown_code() throws Exception {
        given(geoLocationPort.lookupCountry(anyString())).willReturn(CountryCode.of("PL"));
        mockMvc.perform(post("/api/v1/coupons/redeem")
                        .header("X-Forwarded-For", "91.108.0.1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"NOPE","userId":"u1"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("COUPON_NOT_FOUND"));
    }

    @Test
    void redeem_returns_403_when_country_mismatch() throws Exception {
        createCoupon("PLONLY", "PL", 5);
        given(geoLocationPort.lookupCountry(anyString())).willReturn(CountryCode.of("DE"));

        mockMvc.perform(post("/api/v1/coupons/redeem")
                        .header("X-Forwarded-For", "78.46.0.1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"PLONLY","userId":"u1"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("COUNTRY_NOT_ALLOWED"));
    }

    @Test
    void redeem_returns_409_when_already_used_by_same_user() throws Exception {
        createCoupon("ONCE", "PL", 5);
        given(geoLocationPort.lookupCountry(anyString())).willReturn(CountryCode.of("PL"));

        mockMvc.perform(post("/api/v1/coupons/redeem")
                        .header("X-Forwarded-For", "91.108.0.1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"ONCE","userId":"u1"}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/coupons/redeem")
                        .header("X-Forwarded-For", "91.108.0.1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"ONCE","userId":"u1"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("COUPON_ALREADY_USED"));
    }

    @Test
    void redeem_returns_409_when_exhausted() throws Exception {
        createCoupon("LAST", "PL", 1);
        given(geoLocationPort.lookupCountry(anyString())).willReturn(CountryCode.of("PL"));

        mockMvc.perform(post("/api/v1/coupons/redeem")
                        .header("X-Forwarded-For", "91.108.0.1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"LAST","userId":"u1"}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/coupons/redeem")
                        .header("X-Forwarded-For", "91.108.0.2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"LAST","userId":"u2"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("COUPON_EXHAUSTED"));
    }

    @Test
    void create_returns_409_on_duplicate_code() throws Exception {
        createCoupon("DUP", "PL", 5);
        mockMvc.perform(post("/api/v1/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"DUP","maxUses":5,"countryCode":"PL"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("COUPON_CODE_DUPLICATE"));
    }

    @Test
    void create_returns_400_on_invalid_payload() throws Exception {
        mockMvc.perform(post("/api/v1/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"","maxUses":0,"countryCode":"POL"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    void redeem_returns_503_when_geo_fails() throws Exception {
        createCoupon("GEO", "PL", 5);
        given(geoLocationPort.lookupCountry(anyString()))
                .willThrow(new GeoLocationException("upstream down"));

        mockMvc.perform(post("/api/v1/coupons/redeem")
                        .header("X-Forwarded-For", "91.108.0.1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"GEO","userId":"u1"}
                                """))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.errorCode").value("GEOLOCATION_UNAVAILABLE"));
    }

    private void createCoupon(String code, String country, int maxUses) throws Exception {
        mockMvc.perform(post("/api/v1/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"%s","maxUses":%d,"countryCode":"%s"}
                                """.formatted(code, maxUses, country)))
                .andExpect(status().isCreated());
    }
}
