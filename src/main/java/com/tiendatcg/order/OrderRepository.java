package com.tiendatcg.order;

import com.tiendatcg.user.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @EntityGraph(attributePaths = "items")
    List<Order> findByUserOrderByCreatedAtDesc(User user);

    @EntityGraph(attributePaths = "items")
    Optional<Order> findByIdAndUser(Long id, User user);
}
