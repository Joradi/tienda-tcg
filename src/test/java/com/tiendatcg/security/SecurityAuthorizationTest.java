package com.tiendatcg.security;

import com.tiendatcg.card.Card;
import com.tiendatcg.product.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(ProductController.class)
@Import(SecurityConfig.class)
public class SecurityAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Test
    @WithMockUser(username = "cliente@test.com", roles = "CUSTOMER")
    void shouldForbidCustomerFromCreatingProduct() throws Exception {

        mockMvc.perform(
                        post("/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                              "cardId": 5,
                              "language": "ENGLISH",
                              "variant": "NORMAL",
                              "condition": "NEAR_MINT",
                              "stock": 2,
                              "price": 15000
                            }
                            """)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username= "admin@test.com", roles = "ADMIN")
    void shouldAllowAdminToCreateProduct() throws Exception
    {
        Card card = new Card();
        card.setName("Bulbasaur");
        card.setImageUrl("https://example.com/bulbasaur.png");

        Product product = new Product(
                card,
                Language.ENGLISH,
                Variant.NORMAL,
                Condition.NEAR_MINT,
                2,
                15000L,
                LocalDate.now()
        );

        when(productService.createProduct(any(ProductCreateRequest.class)))
                .thenReturn(product);

        mockMvc.perform(
                        post("/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                        {
                          "cardId": 5,
                          "language": "ENGLISH",
                          "variant": "NORMAL",
                          "condition": "NEAR_MINT",
                          "stock": 2,
                          "price": 15000
                        }
                        """)
                )
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn401WhenUnauthenticatedUserCreatesProduct() throws Exception
    {
        mockMvc.perform(
                        post("/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                              "cardId": 5,
                              "language": "ENGLISH",
                              "variant": "NORMAL",
                              "condition": "NEAR_MINT",
                              "stock": 2,
                              "price": 15000
                            }
                            """)
                )
                .andExpect(status().isUnauthorized());
    }
}