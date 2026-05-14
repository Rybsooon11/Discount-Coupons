package com.example.discountcoupons.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "coupon")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 64)
    private String code;

    @Column(name = "country_code", nullable = false, length = 2)
    private CountryCode countryCode;

    @Column(name = "max_uses", nullable = false)
    private int maxUses;

    @Column(name = "current_uses", nullable = false)
    private int currentUses;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private OffsetDateTime createdAt;

    protected Coupon() {
    }

    public Coupon(String code, CountryCode countryCode, int maxUses) {
        this.code = code;
        this.countryCode = countryCode;
        this.maxUses = maxUses;
        this.currentUses = 0;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public CountryCode getCountryCode() {
        return countryCode;
    }

    public int getMaxUses() {
        return maxUses;
    }

    public int getCurrentUses() {
        return currentUses;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
