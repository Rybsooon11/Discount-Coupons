package com.example.discountcoupons.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.OffsetDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String errorCode,
        String message,
        String path,
        List<FieldErrorItem> details
) {
    public record FieldErrorItem(String field, String message) {}
}
