package com.tiendatcg.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SecurityAuthorizationMatrixTest.TestEndpoints.class)
@Import({SecurityConfig.class, SecurityAuthorizationMatrixTest.TestEndpoints.class})
class SecurityAuthorizationMatrixTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void getOrdersWithoutJwtShouldReturnUnauthorized() throws Exception
    {

        mockMvc.perform(get("/orders"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getOrdersWithCustomerJwtShouldBeAllowed() throws Exception
    {

        mockJwt(
                "customer-token",
                "customer@test.com",
                "ROLE_CUSTOMER"
        );

        mockMvc.perform(get("/orders")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer customer-token"))
                .andExpect(status().isOk());
    }

    @Test
    void getProductsWithoutJwtShouldBePublic() throws Exception
    {

        mockMvc.perform(get("/products/test"))
                .andExpect(status().isOk());
    }

    @Test
    void getCartWithoutJwtShouldBePublic() throws Exception
    {
        mockMvc.perform(get("/cart"))
                .andExpect(status().isOk());
    }

    @Test
    void checkoutWithoutJwtShouldBePublic() throws Exception
    {
        mockMvc.perform(post("/checkout"))
                .andExpect(status().isOk());
    }

    @Test
    void customerShouldNotBeAllowedToModifyCards() throws Exception
    {
        mockJwt(
                "customer-token",
                "customer@test.com",
                "ROLE_CUSTOMER"
        );

        mockMvc.perform(post("/cards/test")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer customer-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminShouldBeAllowedToModifyCards() throws Exception
    {
        mockJwt(
                "admin-token",
                "admin@test.com",
                "ROLE_ADMIN"
        );

        mockMvc.perform(post("/cards/test")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token"))
                .andExpect(status().isOk());
    }

    private void mockJwt(String token, String subject, String role)
    {

        Instant now = Instant.now();
        Jwt jwt = Jwt.withTokenValue(token)
                .header("alg", "HS256")
                .subject(subject)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .claim("role", role)
                .build();

        when(jwtDecoder.decode(token))
                .thenReturn(jwt);
    }

    @RestController
    public static class TestEndpoints
    {
        @GetMapping("/orders")
        public void orders()
        {
        }

        @GetMapping("/products/test")
        public void products()
        {
        }

        @GetMapping("/cart")
        public void cart()
        {
        }

        @PostMapping("/checkout")
        public void checkout()
        {
        }

        @PostMapping("/cards/test")
        public void modifyCards()
        {
        }
    }
}