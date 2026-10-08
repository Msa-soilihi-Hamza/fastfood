package com.fastfood.order;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // L'EntityGraph charge client et lignes en une requête (évite le problème N+1)
    @EntityGraph(attributePaths = {"customer", "items"})
    List<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    @EntityGraph(attributePaths = {"customer", "items"})
    List<Order> findByStatusInOrderByCreatedAtAsc(Collection<OrderStatus> statuses);
}
