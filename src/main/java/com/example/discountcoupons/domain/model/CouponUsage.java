package com.example.discountcoupons.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "coupon_usage",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_coupon_usage_coupon_user",
                columnNames = {"coupon_id", "user_id"}
        )
)
public class CouponUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "coupon_id", nullable = false)
    private Long couponId;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(name = "used_at", nullable = false, updatable = false, insertable = false)
    private OffsetDateTime usedAt;

    @Column(name = "used_from_ip", nullable = false, length = 45)
    private String usedFromIp;

    @Column(name = "used_from_country", nullable = false, length = 2)
    private CountryCode usedFromCountry;

    protected CouponUsage() {
    }

    public CouponUsage(Long couponId, String userId, String usedFromIp, CountryCode usedFromCountry) {
        this.couponId = couponId;
        this.userId = userId;
        this.usedFromIp = usedFromIp;
        this.usedFromCountry = usedFromCountry;
    }

    public Long getId() {
        return id;
    }

    public Long getCouponId() {
        return couponId;
    }

    public String getUserId() {
        return userId;
    }

    public OffsetDateTime getUsedAt() {
        return usedAt;
    }

    public String getUsedFromIp() {
        return usedFromIp;
    }

    public CountryCode getUsedFromCountry() {
        return usedFromCountry;
    }
}
