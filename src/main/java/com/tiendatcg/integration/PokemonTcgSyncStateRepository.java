package com.tiendatcg.integration;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PokemonTcgSyncStateRepository extends JpaRepository<PokemonTcgSyncState, Long> {
}
