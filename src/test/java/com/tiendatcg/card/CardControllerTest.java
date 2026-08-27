package com.tiendatcg.card;

import com.tiendatcg.cardset.CardSet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

@WebMvcTest(CardController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CardService cardService;

    @Test
    void shouldSearchCardByName() throws Exception
    {

        CardSet cardSet = new CardSet("base1", "Base", 102);

        Card card = new Card();
        card.setName("Charizard");
        card.setNumber("4");
        card.setImageUrl("https://example.com/charizard.png");
        card.setCardSet(cardSet);

        when(cardService.searchCards("Charizard"))
                .thenReturn(List.of(card));

        mockMvc.perform(
                get("/cards/search")
                        .param("query", "Charizard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Charizard"))
                .andExpect(jsonPath("$[0].number").value("4"))
                .andExpect(jsonPath("$[0].setName").value("Base"))
                .andExpect(jsonPath("$[0].printedTotal").value(102));
    }
}
