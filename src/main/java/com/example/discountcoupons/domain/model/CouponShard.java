package com.example.discountcoupons.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "coupon_shard")
public class CouponShard {

    @EmbeddedId
    private CouponShardId id;

    @Column(name = "current_uses", nullable = false)
    private int currentUses;

    @Column(name = "max_uses", nullable = false)
    private int maxUses;

    protected CouponShard() {
    }

    public CouponShard(Long couponId, short shardId, int maxUses) {
        this.id = new CouponShardId(couponId, shardId);
        this.maxUses = maxUses;
        this.currentUses = 0;
    }

    public CouponShardId getId() {
        return id;
    }

    public int getCurrentUses() {
        return currentUses;
    }

    public int getMaxUses() {
        return maxUses;
    }
}
