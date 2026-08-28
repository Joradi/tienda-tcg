package com.tiendatcg.importation;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ImportationRepository
        extends JpaRepository<Importation, Long> {

    @EntityGraph(attributePaths = {
            "items",
            "items.card"
    })
    Optional<Importation> findWithItemsById(Long id);

    @EntityGraph(attributePaths = {
            "items",
            "items.card"
    })
    List<Importation> findAllByOrderByCreatedAtDesc();
}