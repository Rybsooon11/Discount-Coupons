package com.example.discountcoupons.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public record CouponShardId(
        @Column(name = "coupon_id", nullable = false) Long couponId,
        @Column(name = "shard_id", nullable = false) Short shardId
) implements Serializable {
}
