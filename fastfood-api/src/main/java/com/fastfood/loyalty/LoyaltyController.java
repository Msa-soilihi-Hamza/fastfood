package com.fastfood.loyalty;

import com.fastfood.auth.AuthUser;
import com.fastfood.loyalty.dto.LoyaltyDtos.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class LoyaltyController {

    private final LoyaltyService loyaltyService;

    // --- Client ---

    @GetMapping("/api/rewards")
    public List<RewardResponse> rewards() {
        return loyaltyService.activeRewards();
    }

    @GetMapping("/api/loyalty/me")
    public MyLoyaltyResponse me(@AuthenticationPrincipal AuthUser user) {
        return loyaltyService.myLoyalty(user);
    }

    // --- Restaurateur ---

    @GetMapping("/api/admin/loyalty/customers/{code}")
    public CustomerLookupResponse lookup(@PathVariable String code) {
        return loyaltyService.lookup(code);
    }

    @PostMapping("/api/admin/loyalty/redeem")
    public RedeemResponse redeem(@Valid @RequestBody RedeemRequest request) {
        return loyaltyService.redeem(request);
    }

    @GetMapping("/api/admin/rewards")
    public List<RewardResponse> allRewards() {
        return loyaltyService.allRewards();
    }

    @PostMapping("/api/admin/rewards")
    @ResponseStatus(HttpStatus.CREATED)
    public RewardResponse createReward(@Valid @RequestBody RewardRequest request) {
        return loyaltyService.createReward(request);
    }

    @PutMapping("/api/admin/rewards/{id}")
    public RewardResponse updateReward(@PathVariable Long id, @Valid @RequestBody RewardRequest request) {
        return loyaltyService.updateReward(id, request);
    }
}
