package com.tiendatcg.integration;

import com.tiendatcg.card.CardResponse;
import com.tiendatcg.cardset.CardSetResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PokemonTcgClient {
    private final RestClient restClient;

    public PokemonTcgClient(@Value("${pokemon.tcg.base-url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public CardSetResponse getCardSets()
    {
        return restClient.get()
                .uri("/sets")
                .retrieve()
                .body(CardSetResponse.class);
    }

    public CardResponse getCards(int page)
    {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/cards")
                        .queryParam("page", page)
                        .queryParam("pageSize", 250)
                        .queryParam(
                                "select",
                                "id,name,supertype,subtypes,artist,number,rarity,images,set"
                        )
                        .build())
                .retrieve()
                .body(CardResponse.class);
    }
}
