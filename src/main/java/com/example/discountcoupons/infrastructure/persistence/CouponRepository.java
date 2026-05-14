package com.example.discountcoupons.infrastructure.persistence;

import com.example.discountcoupons.domain.model.Coupon;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCode(String code);

    boolean existsByCode(String code);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Coupon c
               SET c.currentUses = c.currentUses + 1
             WHERE c.id = :id
               AND c.currentUses < c.maxUses
            """)
    int tryIncrementUsage(@Param("id") Long couponId);
}
