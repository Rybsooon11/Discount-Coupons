package com.example.discountcoupons.domain.model;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public record CountryCode(String value) {

    private static final Pattern ISO_3166_ALPHA_2 = Pattern.compile("^[A-Z]{2}$");

    public CountryCode {
        Objects.requireNonNull(value, "countryCode value is required");
        if (!ISO_3166_ALPHA_2.matcher(value).matches()) {
            throw new IllegalArgumentException(
                    "countryCode must match ISO 3166-1 alpha-2 (uppercase): " + value);
        }
    }

    public static CountryCode of(String raw) {
        Objects.requireNonNull(raw, "countryCode value is required");
        return new CountryCode(raw.trim().toUpperCase(Locale.ROOT));
    }
}
