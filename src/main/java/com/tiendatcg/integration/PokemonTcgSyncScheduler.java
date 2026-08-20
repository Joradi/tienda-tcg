package com.tiendatcg.integration;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PokemonTcgSyncScheduler {
    private final PokemonTcgSyncService pokemonTcgSyncService;

    public PokemonTcgSyncScheduler(PokemonTcgSyncService pokemonTcgSyncService) {
        this.pokemonTcgSyncService = pokemonTcgSyncService;
    }

    @Scheduled(cron = "0 0 3 * * SUN", zone = "America/Santiago")
    public void syncWeeklyCatalog() {
        try {
            pokemonTcgSyncService.syncCatalog();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
