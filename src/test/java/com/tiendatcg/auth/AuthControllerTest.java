package com.tiendatcg.auth;

import com.tiendatcg.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void shouldLoginAndReturnToken() throws Exception
    {
        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(authentication);

        when(jwtService.generateToken(authentication))
                .thenReturn("token-de-prueba");

        mockMvc.perform(
                post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "email": "cliente@test.com",
                                "password": "claveSegura123"
                                }
                                """)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-de-prueba"));
    }

    @Test
    void shouldReturn400WhenLoginRequestIsInvalid() throws Exception
    {
        mockMvc.perform(
                post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                        {
                                        "email": "no-es-un-email",
                                        "password": ""
                                        }
                                        """
                        )
        )
                .andExpect(status().isBadRequest());
    }

}
