package com.tiendatcg.card;

import com.tiendatcg.cardset.CardSet;
import com.tiendatcg.cardset.CardSetService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardSetService cardSetService;

    @InjectMocks
    private CardService cardService;

    @Test
    void shouldFindCardIgnoringLeadingZeros()
    {
        Card card = new Card();
        card.setNumber("4");
        card.setName("Charizard");

        when(cardRepository.findByCardSet_PrintedTotal(102))
                .thenReturn(List.of(card));

        List<Card> result = cardService.searchCards("004/102");

        assertEquals(1, result.size());
        assertEquals("Charizard", result.getFirst().getName());

    }

    @Test
    void shouldFindCardByNameIgnorringCase()
    {
        Card card = new Card();
        card.setName("Charizard");

        when(cardRepository.findByNameContainingIgnoreCase("charizard"))
                .thenReturn(List.of(card));

        List<Card> result = cardService.searchCards("charizard");

        assertEquals(1, result.size());
        assertEquals("Charizard", result.getFirst().getName());
    }
}
