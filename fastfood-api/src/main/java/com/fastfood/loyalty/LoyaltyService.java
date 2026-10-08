package com.fastfood.loyalty;

import com.fastfood.auth.AuthUser;
import com.fastfood.auth.User;
import com.fastfood.auth.UserRepository;
import com.fastfood.common.ApiException;
import com.fastfood.loyalty.dto.LoyaltyDtos.*;
import com.fastfood.order.Order;
import com.fastfood.restaurant.SettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class LoyaltyService {

    private final UserRepository users;
    private final RewardRepository rewards;
    private final PointTransactionRepository transactions;
    private final SettingsService settingsService;

    /** Points gagnés pour un montant : arrondi à l'inférieur (9,90 € à 1 pt/€ = 9 pts). */
    public static int pointsFor(BigDecimal amount, int pointsPerEuro) {
        return amount.multiply(BigDecimal.valueOf(pointsPerEuro))
                .setScale(0, RoundingMode.FLOOR)
                .intValueExact();
    }

    /** Crédite le client d'une commande remise. Appelé une seule fois par commande. */
    @Transactional
    public int creditForOrder(Order order) {
        if (transactions.existsByOrderId(order.getId())) {
            throw ApiException.conflict("Les points de cette commande ont déjà été crédités");
        }
        int points = pointsFor(order.getTotal(), settingsService.get().getPointsPerEuro());
        if (points > 0) {
            User customer = order.getCustomer();
            customer.addPoints(points);
            transactions.save(PointTransaction.earned(customer, points, order));
        }
        return points;
    }

    @Transactional
    public RedeemResponse redeem(RedeemRequest request) {
        User customer = findByCode(request.loyaltyCode());
        Reward reward = rewards.findById(request.rewardId())
                .filter(Reward::isActive)
                .orElseThrow(() -> ApiException.notFound("Cadeau introuvable ou désactivé"));

        customer.spendPoints(reward.getCostPoints());
        transactions.save(PointTransaction.redeemed(customer, reward));
        return new RedeemResponse(reward.getName(), reward.getCostPoints(), customer.getPoints());
    }

    @Transactional(readOnly = true)
    public MyLoyaltyResponse myLoyalty(AuthUser authUser) {
        User user = users.findById(authUser.id())
                .orElseThrow(() -> ApiException.notFound("Utilisateur introuvable"));
        List<TransactionResponse> history = transactions.findTop50ByUserIdOrderByCreatedAtDesc(user.getId())
                .stream().map(TransactionResponse::from).toList();
        return new MyLoyaltyResponse(user.getLoyaltyCode(), user.getPoints(), history);
    }

    @Transactional(readOnly = true)
    public CustomerLookupResponse lookup(String loyaltyCode) {
        return CustomerLookupResponse.from(findByCode(loyaltyCode));
    }

    @Transactional(readOnly = true)
    public List<RewardResponse> activeRewards() {
        return rewards.findByActiveTrueOrderByCostPointsAsc().stream().map(RewardResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<RewardResponse> allRewards() {
        return rewards.findAllByOrderByCostPointsAsc().stream().map(RewardResponse::from).toList();
    }

    @Transactional
    public RewardResponse createReward(RewardRequest request) {
        Reward reward = new Reward();
        applyReward(reward, request);
        return RewardResponse.from(rewards.save(reward));
    }

    @Transactional
    public RewardResponse updateReward(Long id, RewardRequest request) {
        Reward reward = rewards.findById(id).orElseThrow(() -> ApiException.notFound("Cadeau introuvable : " + id));
        applyReward(reward, request);
        return RewardResponse.from(reward);
    }

    User findByCode(String loyaltyCode) {
        return users.findByLoyaltyCode(normalizeCode(loyaltyCode))
                .orElseThrow(() -> ApiException.notFound("Code client inconnu"));
    }

    public static String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private static void applyReward(Reward reward, RewardRequest request) {
        reward.setName(request.name().trim());
        reward.setCostPoints(request.costPoints());
        reward.setActive(request.active());
    }
}
