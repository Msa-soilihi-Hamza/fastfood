package com.fastfood.loyalty;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {

    List<PointTransaction> findTop50ByUserIdOrderByCreatedAtDesc(Long userId);

    boolean existsByOrderId(Long orderId);
}
