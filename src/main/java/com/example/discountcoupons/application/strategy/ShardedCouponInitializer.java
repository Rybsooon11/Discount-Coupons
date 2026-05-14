package com.example.discountcoupons.application.strategy;

import com.example.discountcoupons.domain.model.Coupon;
import com.example.discountcoupons.domain.model.CouponShard;
import com.example.discountcoupons.infrastructure.persistence.CouponShardRepository;
import java.util.ArrayList;
import java.util.List;

public class ShardedCouponInitializer implements CouponInitializer {

    private final CouponShardRepository shardRepository;
    private final int shardCount;

    public ShardedCouponInitializer(CouponShardRepository shardRepository, int shardCount) {
        if (shardCount <= 0) {
            throw new IllegalArgumentException("shardCount must be > 0");
        }
        this.shardRepository = shardRepository;
        this.shardCount = shardCount;
    }

    @Override
    public void initialize(Coupon coupon) {
        int total = coupon.getMaxUses();
        int base = total / shardCount;
        int remainder = total % shardCount;
        List<CouponShard> shards = new ArrayList<>(shardCount);
        for (short s = 0; s < shardCount; s++) {
            int max = base + (s < remainder ? 1 : 0);
            if (max > 0) {
                shards.add(new CouponShard(coupon.getId(), s, max));
            }
        }
        shardRepository.saveAll(shards);
        shardRepository.flush();
    }
}
