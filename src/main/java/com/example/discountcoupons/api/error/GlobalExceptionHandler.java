package com.example.discountcoupons.api.error;

import com.example.discountcoupons.api.dto.ErrorResponse;
import com.example.discountcoupons.api.dto.ErrorResponse.FieldErrorItem;
import com.example.discountcoupons.domain.exception.CountryNotAllowedException;
import com.example.discountcoupons.domain.exception.CouponAlreadyUsedException;
import com.example.discountcoupons.domain.exception.CouponCodeDuplicateException;
import com.example.discountcoupons.domain.exception.CouponExhaustedException;
import com.example.discountcoupons.domain.exception.CouponNotFoundException;
import com.example.discountcoupons.domain.exception.GeoLocationException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(CouponNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(CouponNotFoundException ex, HttpServletRequest req) {
        log.info("Coupon not found: {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, "COUPON_NOT_FOUND", "Coupon not found", req, null);
    }

    @ExceptionHandler(CouponExhaustedException.class)
    public ResponseEntity<ErrorResponse> handleExhausted(CouponExhaustedException ex, HttpServletRequest req) {
        log.info("Coupon exhausted: {}", ex.getMessage());
        return build(HttpStatus.CONFLICT, "COUPON_EXHAUSTED", "Coupon exhausted", req, null);
    }

    @ExceptionHandler(CouponAlreadyUsedException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyUsed(CouponAlreadyUsedException ex, HttpServletRequest req) {
        log.info("Coupon already used: {}", ex.getMessage());
        return build(HttpStatus.CONFLICT, "COUPON_ALREADY_USED", "Coupon already used", req, null);
    }

    @ExceptionHandler(CouponCodeDuplicateException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(CouponCodeDuplicateException ex, HttpServletRequest req) {
        log.info("Duplicate coupon code: {}", ex.getMessage());
        return build(HttpStatus.CONFLICT, "COUPON_CODE_DUPLICATE", "Coupon code already exists", req, null);
    }

    @ExceptionHandler(CountryNotAllowedException.class)
    public ResponseEntity<ErrorResponse> handleCountry(CountryNotAllowedException ex, HttpServletRequest req) {
        log.info("Country not allowed: {}", ex.getMessage());
        return build(HttpStatus.FORBIDDEN, "COUNTRY_NOT_ALLOWED", "Country not allowed", req, null);
    }

    @ExceptionHandler(GeoLocationException.class)
    public ResponseEntity<ErrorResponse> handleGeo(GeoLocationException ex, HttpServletRequest req) {
        log.warn("Geolocation unavailable: {}", ex.getMessage());
        return build(HttpStatus.SERVICE_UNAVAILABLE, "GEOLOCATION_UNAVAILABLE",
                "Geolocation service unavailable", req, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                          HttpServletRequest req) {
        List<FieldErrorItem> details = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldError)
                .toList();
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Validation failed", req, details);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex,
                                                          HttpServletRequest req) {
        log.info("Malformed JSON body");
        return build(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", "Malformed request body", req, null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleIntegrity(DataIntegrityViolationException ex,
                                                         HttpServletRequest req) {
        log.warn("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());
        return build(HttpStatus.CONFLICT, "DATA_INTEGRITY_VIOLATION",
                "Data integrity violation", req, null);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandler(NoHandlerFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "NOT_FOUND", "Resource not found", req, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Internal server error", req, null);
    }

    private FieldErrorItem toFieldError(FieldError fe) {
        return new FieldErrorItem(fe.getField(), fe.getDefaultMessage());
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String code, String message,
                                                HttpServletRequest req, List<FieldErrorItem> details) {
        ErrorResponse body = new ErrorResponse(
                OffsetDateTime.now(),
                status.value(),
                code,
                message,
                req.getRequestURI(),
                details
        );
        return ResponseEntity.status(status).body(body);
    }
}
