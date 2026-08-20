package com.tiendatcg.integration;

import com.tiendatcg.card.CardResponse;
import com.tiendatcg.card.CardService;
import com.tiendatcg.cardset.CardSetResponse;
import com.tiendatcg.cardset.CardSetService;
import org.springframework.stereotype.Service;

@Service
public class PokemonTcgSyncService {

    private final PokemonTcgClient pokemonTcgClient;
    private final CardService cardService;
    private final PokemonTcgSyncStateRepository syncStateRepository;
    private final CardSetService cardSetService;

    public PokemonTcgSyncService(PokemonTcgClient pokemonTcgClient, CardService cardService, CardSetService cardSetService, PokemonTcgSyncStateRepository syncStateRepository) {
        this.pokemonTcgClient = pokemonTcgClient;
        this.cardService = cardService;
        this.cardSetService = cardSetService;
        this.syncStateRepository = syncStateRepository;
    }

    public void syncCatalog() throws InterruptedException
    {
        CardSetResponse cardSetResponse = pokemonTcgClient.getCardSets();
        cardSetService.syncCardSets(cardSetResponse);
        syncAllCards();
    }

    public void syncAllCards() throws InterruptedException
    {
       PokemonTcgSyncState syncState = syncStateRepository.findById(1L)
               .orElseGet(() -> {
                   PokemonTcgSyncState state = new PokemonTcgSyncState();
                   state.setId(1L);
                   state.setLastCompletedPage(0);
                   return syncStateRepository.save(state);
               });

       int page = syncState.getLastCompletedPage() + 1;

        while(true) {

            CardResponse response = null;
            int attempts = 0;

            while (response == null && attempts < 3)
            {
                try {
                    response = pokemonTcgClient.getCards(page);
                }
                catch (Exception e)
                {
                    attempts++;

                    System.out.println(
                            "Error en la página " + page +
                                    ". Intento " + attempts + " de 3"
                    );

                    Thread.sleep(5000);
                }

            }

            if(response == null)
            {
                System.out.println("No se pudo descargar la página " + page +
                        ". Sincronización detenida.");
                break;
            }

            cardService.syncCards(response);

            syncState.setLastCompletedPage(page);
            syncStateRepository.save(syncState);

            System.out.println(
                    "Página " + page +
                            " sincronizada - cartas: " + response.getCount()
            );

            System.out.println(
                    "Total cartas API: " + response.getTotalCount()
            );

            if( page * response.getPageSize() >= response.getTotalCount())
            {
                syncState.setLastCompletedPage(0);
                syncStateRepository.save(syncState);

                break;
            }

            page++;

            Thread.sleep(2100);
        }
    }
}
