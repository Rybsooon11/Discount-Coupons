package com.example.discountcoupons.infrastructure.persistence;

import com.example.discountcoupons.domain.model.CouponUsage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponUsageRepository extends JpaRepository<CouponUsage, Long> {

    long countByCouponId(Long couponId);
}
