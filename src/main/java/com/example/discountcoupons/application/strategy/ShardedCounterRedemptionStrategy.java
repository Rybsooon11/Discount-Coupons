package com.example.discountcoupons.application.strategy;

import com.example.discountcoupons.domain.model.Coupon;
import com.example.discountcoupons.infrastructure.persistence.CouponShardRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ShardedCounterRedemptionStrategy implements CouponRedemptionStrategy {

    private final CouponShardRepository shardRepository;
    private final int shardCount;
    private final int fallbackAttempts;

    public ShardedCounterRedemptionStrategy(CouponShardRepository shardRepository,
                                            int shardCount,
                                            int fallbackAttempts) {
        if (shardCount <= 0) {
            throw new IllegalArgumentException("shardCount must be > 0");
        }
        if (fallbackAttempts < 0) {
            throw new IllegalArgumentException("fallbackAttempts must be >= 0");
        }
        this.shardRepository = shardRepository;
        this.shardCount = shardCount;
        this.fallbackAttempts = fallbackAttempts;
    }

    @Override
    public boolean tryRedeem(Coupon coupon, String userId) {
        Long couponId = coupon.getId();
        short primary = (short) (Math.floorMod(userId.hashCode(), shardCount));
        if (shardRepository.tryIncrementShard(couponId, primary) > 0) {
            return true;
        }
        for (int attempt = 0; attempt < fallbackAttempts; attempt++) {
            short s = (short) ThreadLocalRandom.current().nextInt(shardCount);
            if (shardRepository.tryIncrementShard(couponId, s) > 0) {
                return true;
            }
        }
        long used = shardRepository.sumCurrentUses(couponId);
        long max = shardRepository.sumMaxUses(couponId);
        if (used >= max) {
            return false;
        }
        List<Short> order = new ArrayList<>(shardCount);
        for (short s = 0; s < shardCount; s++) {
            order.add(s);
        }
        Collections.shuffle(order, ThreadLocalRandom.current());
        for (short s : order) {
            if (shardRepository.tryIncrementShard(couponId, s) > 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int computeRemainingUses(Coupon coupon) {
        long max = shardRepository.sumMaxUses(coupon.getId());
        long used = shardRepository.sumCurrentUses(coupon.getId());
        long remaining = max - used;
        return remaining < 0 ? 0 : (int) remaining;
    }
}
