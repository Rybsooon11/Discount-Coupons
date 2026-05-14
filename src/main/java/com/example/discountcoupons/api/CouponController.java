package com.example.discountcoupons.api;

import com.example.discountcoupons.api.dto.CouponResponse;
import com.example.discountcoupons.api.dto.CreateCouponRequest;
import com.example.discountcoupons.api.dto.RedeemCouponRequest;
import com.example.discountcoupons.api.dto.RedeemCouponResponse;
import com.example.discountcoupons.api.util.ClientIpResolver;
import com.example.discountcoupons.application.CouponService;
import com.example.discountcoupons.application.CouponService.RedemptionResult;
import com.example.discountcoupons.application.CouponView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/coupons")
public class CouponController {

    private final CouponService couponService;
    private final ClientIpResolver clientIpResolver;

    public CouponController(CouponService couponService, ClientIpResolver clientIpResolver) {
        this.couponService = couponService;
        this.clientIpResolver = clientIpResolver;
    }

    @PostMapping
    public ResponseEntity<CouponResponse> create(@Valid @RequestBody CreateCouponRequest request) {
        CouponView view = couponService.createCoupon(request.code(), request.countryCode(), request.maxUses());
        return ResponseEntity
                .created(URI.create("/api/v1/coupons/" + view.code()))
                .body(CouponResponse.from(view));
    }

    @PostMapping("/redeem")
    public RedeemCouponResponse redeem(@Valid @RequestBody RedeemCouponRequest request,
                                       HttpServletRequest httpRequest) {
        String ip = clientIpResolver.resolve(httpRequest);
        RedemptionResult result = couponService.redeem(request.code(), request.userId(), ip);
        return new RedeemCouponResponse(result.code(), result.remainingUses());
    }
}
