package com.example.discountcoupons.infrastructure.persistence;

import com.example.discountcoupons.domain.model.CouponShard;
import com.example.discountcoupons.domain.model.CouponShardId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CouponShardRepository extends JpaRepository<CouponShard, CouponShardId> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE CouponShard s
               SET s.currentUses = s.currentUses + 1
             WHERE s.id.couponId = :couponId
               AND s.id.shardId = :shardId
               AND s.currentUses < s.maxUses
            """)
    int tryIncrementShard(@Param("couponId") Long couponId,
                          @Param("shardId") Short shardId);

    @Query("SELECT COALESCE(SUM(s.currentUses), 0) FROM CouponShard s WHERE s.id.couponId = :couponId")
    long sumCurrentUses(@Param("couponId") Long couponId);

    @Query("SELECT COALESCE(SUM(s.maxUses), 0) FROM CouponShard s WHERE s.id.couponId = :couponId")
    long sumMaxUses(@Param("couponId") Long couponId);
}
