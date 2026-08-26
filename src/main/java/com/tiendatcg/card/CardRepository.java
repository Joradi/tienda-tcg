package com.tiendatcg.card;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {

    Optional<Card> findByExternalId(String externalId);
    List<Card> findByNameContainingIgnoreCase(String name);
    List<Card> findByCardSet_PrintedTotal(int printedTotal);
}
