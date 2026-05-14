package com.example.discountcoupons.infrastructure.config;

import com.example.discountcoupons.application.strategy.AtomicUpdateRedemptionStrategy;
import com.example.discountcoupons.application.strategy.CouponInitializer;
import com.example.discountcoupons.application.strategy.CouponRedemptionStrategy;
import com.example.discountcoupons.application.strategy.ShardedCounterRedemptionStrategy;
import com.example.discountcoupons.application.strategy.ShardedCouponInitializer;
import com.example.discountcoupons.infrastructure.persistence.CouponRepository;
import com.example.discountcoupons.infrastructure.persistence.CouponShardRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CouponStrategyConfig {

    @Bean
    @ConditionalOnProperty(name = "coupons.strategy", havingValue = "atomic-update", matchIfMissing = true)
    public CouponRedemptionStrategy atomicUpdateStrategy(CouponRepository couponRepository) {
        return new AtomicUpdateRedemptionStrategy(couponRepository);
    }

    @Bean
    @ConditionalOnProperty(name = "coupons.strategy", havingValue = "sharded-counter")
    public CouponRedemptionStrategy shardedCounterStrategy(
            CouponShardRepository shardRepository,
            @Value("${coupons.sharded.shards:16}") int shardCount,
            @Value("${coupons.sharded.fallback-attempts:3}") int fallbackAttempts) {
        return new ShardedCounterRedemptionStrategy(shardRepository, shardCount, fallbackAttempts);
    }

    @Bean
    @ConditionalOnProperty(name = "coupons.strategy", havingValue = "sharded-counter")
    public CouponInitializer shardedCouponInitializer(
            CouponShardRepository shardRepository,
            @Value("${coupons.sharded.shards:16}") int shardCount) {
        return new ShardedCouponInitializer(shardRepository, shardCount);
    }
}
