package com.example.discountcoupons.infrastructure.persistence;

import com.example.discountcoupons.domain.model.CountryCode;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CountryCodeConverter implements AttributeConverter<CountryCode, String> {

    @Override
    public String convertToDatabaseColumn(CountryCode attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public CountryCode convertToEntityAttribute(String dbData) {
        return dbData == null ? null : new CountryCode(dbData);
    }
}
