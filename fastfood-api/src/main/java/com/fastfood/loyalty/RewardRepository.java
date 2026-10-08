package com.fastfood.loyalty;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RewardRepository extends JpaRepository<Reward, Long> {

    List<Reward> findByActiveTrueOrderByCostPointsAsc();

    List<Reward> findAllByOrderByCostPointsAsc();
}
