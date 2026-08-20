package com.tiendatcg.integration;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "pokemon_tcg_sync_state")
public class PokemonTcgSyncState {

    @Id
    private Long id;
    private int lastCompletedPage;

    public PokemonTcgSyncState() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getLastCompletedPage() {
        return lastCompletedPage;
    }

    public void setLastCompletedPage(int lastCompletedPage) {
        this.lastCompletedPage = lastCompletedPage;
    }
}
