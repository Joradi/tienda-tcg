package com.tiendatcg.integration;

import com.tiendatcg.cardset.CardSetResponse;
import com.tiendatcg.cardset.CardSetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class PokemonTcgClientTest {

    @Autowired
    private PokemonTcgClient pokemonTcgClient;

    @Autowired
    private CardSetService cardSetService;

    @Autowired
    private PokemonTcgSyncService pokemonTcgSyncService;

    @Test
    void getCardSets()
    {
        CardSetResponse response = pokemonTcgClient.getCardSets();

        System.out.println("Total sets: " + response.getTotalCount());
        System.out.println("Recibidos: " + response.getCount());
        System.out.println("Primer set: " + response.getData().getFirst().getName());
    }

    @Test
    void syncCardSets()
    {
        CardSetResponse response = pokemonTcgClient.getCardSets();

        cardSetService.syncCardSets(response);

        System.out.println("Sets sincronizados: " + response.getCount());
    }

    @Test
    void syncAllCards() throws InterruptedException
    {
        pokemonTcgSyncService.syncAllCards();
    }
}
