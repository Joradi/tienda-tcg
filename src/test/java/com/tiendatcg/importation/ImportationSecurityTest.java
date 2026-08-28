package com.tiendatcg.importation;

import com.tiendatcg.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ImportationController.class)
@Import(SecurityConfig.class)
class ImportationSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ImportationService importationService;

    @MockitoBean
    private ImportAnalysisService analysisService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void shouldReturn401WhenUserIsNotAuthenticated() throws Exception
    {
        mockMvc.perform(get("/importations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn403WhenCustomerAccessesImportations() throws Exception
    {
        Instant now = Instant.now();

        Jwt customerJwt = Jwt.withTokenValue("customer-token")
                        .header("alg", "HS256")
                        .subject("customer@test.com")
                        .issuedAt(now)
                        .expiresAt(now.plusSeconds(3600))
                        .claim(
                                "role",
                                "ROLE_CUSTOMER"
                        )
                        .build();

        when(jwtDecoder.decode("customer-token")).thenReturn(customerJwt);

        mockMvc.perform(get("/importations").header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer customer-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAdminToAccessImportations() throws Exception
    {
        Instant now = Instant.now();

        Jwt adminJwt = Jwt.withTokenValue("admin-token").header(
                                "alg",
                                "HS256"
                        )
                        .subject("admin@test.com")
                        .issuedAt(now)
                        .expiresAt(now.plusSeconds(3600))
                        .claim(
                                "role",
                                "ROLE_ADMIN"
                        )
                        .build();

        when(jwtDecoder.decode("admin-token")).thenReturn(adminJwt);

        when(importationService.getImportations()).thenReturn(List.of());

        mockMvc.perform(get("/importations").header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer admin-token"))
                .andExpect(status().isOk());
    }
}