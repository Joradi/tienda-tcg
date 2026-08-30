package com.tiendatcg.product;

import com.tiendatcg.card.Card;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ProductControllerTest
{
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Test
    void shouldReturnProduct() throws Exception
    {
        Card card = mock(Card.class, RETURNS_DEEP_STUBS);

        when(card.getName()).thenReturn("Bulbasaur");

        when(card.getImageUrl()).thenReturn("https://example.com/bulbasaur.png");

        when(card.getNumber()).thenReturn("44");

        when(card.getCardSet().getName()).thenReturn("Base Set");

        when(card.getCardSet().getPrintedTotal()).thenReturn(102);

        when(card.getIllustrator()).thenReturn("Mitsuhiro Arita");

        when(card.getRarity()).thenReturn("Common");

        when(card.getSuperType()).thenReturn("Pokémon");

        Product product = new Product(card, Language.ENGLISH, Variant.NORMAL, Condition.NEAR_MINT, 10,15000L, LocalDate.now());

        when(productService.getProductById(2L)).thenReturn(product);

        mockMvc.perform(get("/products/2")).andExpect(status().isOk()).andExpect(jsonPath("$.cardName").value("Bulbasaur"))
                .andExpect(jsonPath("$.cardNumber").value("44"))
                .andExpect(jsonPath("$.setName").value("Base Set"))
                .andExpect(jsonPath("$.setPrintedTotal").value(102))
                .andExpect(jsonPath("$.illustrator").value("Mitsuhiro Arita"))
                .andExpect(jsonPath("$.rarity").value("Common"))
                .andExpect(jsonPath("$.superType").value("Pokémon"))
                .andExpect(jsonPath("$.stock").value(10))
                .andExpect(jsonPath("$.price").value(15000L));
    }

    @Test
    void shouldReturn404WhenProductDoesNotExist() throws Exception
    {
        when(productService.getProductById(999999L)).thenThrow(
                        new ProductNotFoundException("Producto no encontrado"));

        mockMvc.perform(get("/products/999999")).andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn400WhenProductRequestIsInvalid() throws Exception
    {
        mockMvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "cardId": 5,
                                          "language": "ENGLISH",
                                          "variant": "NORMAL",
                                          "condition": "NEAR_MINT",
                                          "stock": -1,
                                          "price": 15000
                                        }
                                        """))
                .andExpect(status().isBadRequest());
    }
}