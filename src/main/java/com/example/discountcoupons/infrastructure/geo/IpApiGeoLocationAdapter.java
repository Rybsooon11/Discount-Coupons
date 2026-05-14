package com.example.discountcoupons.infrastructure.geo;

import com.example.discountcoupons.application.port.GeoLocationPort;
import com.example.discountcoupons.domain.exception.GeoLocationException;
import com.example.discountcoupons.domain.model.CountryCode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component("ipApiGeoLocationAdapter")
public class IpApiGeoLocationAdapter implements GeoLocationPort {

    private final WebClient geoWebClient;

    public IpApiGeoLocationAdapter(WebClient geoWebClient) {
        this.geoWebClient = geoWebClient;
    }

    @Override
    public CountryCode lookupCountry(String ip) {
        try {
            IpApiResponse response = geoWebClient.get()
                    .uri("/json/{ip}?fields=status,countryCode,message", ip)
                    .retrieve()
                    .bodyToMono(IpApiResponse.class)
                    .block();

            if (response == null || !"success".equalsIgnoreCase(response.status())) {
                throw new GeoLocationException("Geolocation failed");
            }
            if (response.countryCode() == null || response.countryCode().isBlank()) {
                throw new GeoLocationException("Missing countryCode in geolocation response");
            }
            return CountryCode.of(response.countryCode());
        } catch (GeoLocationException ex) {
            throw ex;
        } catch (IllegalArgumentException ex) {
            throw new GeoLocationException("Invalid countryCode in geolocation response");
        } catch (RuntimeException ex) {
            throw new GeoLocationException(ip, ex);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record IpApiResponse(String status, String countryCode, String message) {}
}
