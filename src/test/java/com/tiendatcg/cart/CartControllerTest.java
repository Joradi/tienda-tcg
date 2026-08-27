package com.tiendatcg.cart;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;



@WebMvcTest(CartController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private CartMapper cartMapper;

    @Test
    void shouldCreateGuestCartWhenNoGuestTokenIsProvided() throws Exception {

        UUID guestToken = UUID.randomUUID();

        Cart cart = new Cart(guestToken);

        CartResponseDto response = new CartResponseDto(
                1L,
                guestToken,
                List.of(),
                0L,
                0L,
                0L
        );

        when(cartService.getOrCreateGuestCart(null))
                .thenReturn(cart);

        when(cartMapper.toResponse(cart))
                .thenReturn(response);

        mockMvc.perform(get("/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guestToken").value(guestToken.toString()))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.total").value(0));

        verify(cartService).getOrCreateGuestCart(null);
        verify(cartMapper).toResponse(cart);
    }

    @Test
    void shouldReturnExistingGuestCartWhenGuestTokenIsProvided() throws Exception {

        UUID guestToken = UUID.randomUUID();

        Cart cart = new Cart(guestToken);

        CartResponseDto response = new CartResponseDto(
                1L,
                guestToken,
                List.of(),
                0L,
                0L,
                0L
        );

        when(cartService.getOrCreateGuestCart(guestToken))
                .thenReturn(cart);

        when(cartMapper.toResponse(cart))
                .thenReturn(response);

        mockMvc.perform(
                        get("/cart")
                                .header(
                                        "X-Guest-Cart-Token",
                                        guestToken.toString()
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guestToken").value(guestToken.toString()));

        verify(cartService).getOrCreateGuestCart(guestToken);
        verify(cartMapper).toResponse(cart);
    }

    @Test
    void shouldAddItemToGuestCart() throws Exception {

        UUID guestToken = UUID.randomUUID();

        Cart cart = new Cart(guestToken);
        Cart updatedCart = new Cart(guestToken);

        CartResponseDto response = new CartResponseDto(
                1L,
                guestToken,
                List.of(),
                0L,
                0L,
                15000L
        );

        when(cartService.getOrCreateGuestCart(guestToken))
                .thenReturn(cart);

        when(cartService.addProduct(cart, 5L, 2))
                .thenReturn(updatedCart);

        when(cartMapper.toResponse(updatedCart))
                .thenReturn(response);

        mockMvc.perform(
                        post("/cart/items")
                                .header(
                                        "X-Guest-Cart-Token",
                                        guestToken.toString()
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                        {
                          "productId": 5,
                          "quantity": 2
                        }
                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(15000));

        verify(cartService).getOrCreateGuestCart(guestToken);
        verify(cartService).addProduct(cart, 5L, 2);
        verify(cartMapper).toResponse(updatedCart);
    }

    @Test
    void shouldUpdateItemQuantityInGuestCart() throws Exception {

        UUID guestToken = UUID.randomUUID();

        Cart cart = new Cart(guestToken);
        Cart updatedCart = new Cart(guestToken);

        CartResponseDto response = new CartResponseDto(
                1L,
                guestToken,
                List.of(),
                0L,
                0L,
                15000L
        );

        when(cartService.getOrCreateGuestCart(guestToken))
                .thenReturn(cart);

        when(cartService.updateQuantity(cart, 5L, 3))
                .thenReturn(updatedCart);

        when(cartMapper.toResponse(updatedCart))
                .thenReturn(response);

        mockMvc.perform(
                        patch("/cart/items/5")
                                .header(
                                        "X-Guest-Cart-Token",
                                        guestToken.toString()
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                              "quantity": 3
                            }
                            """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(15000));

        verify(cartService).getOrCreateGuestCart(guestToken);
        verify(cartService).updateQuantity(cart, 5L, 3);
        verify(cartMapper).toResponse(updatedCart);
    }

    @Test
    void shouldRemoveItemFromGuestCart() throws Exception {

        UUID guestToken = UUID.randomUUID();

        Cart cart = new Cart(guestToken);
        Cart updatedCart = new Cart(guestToken);

        CartResponseDto response = new CartResponseDto(
                1L,
                guestToken,
                List.of(),
                0L,
                0L,
                0L
        );

        when(cartService.getOrCreateGuestCart(guestToken))
                .thenReturn(cart);

        when(cartService.removeProduct(cart, 5L))
                .thenReturn(updatedCart);

        when(cartMapper.toResponse(updatedCart))
                .thenReturn(response);

        mockMvc.perform(
                        delete("/cart/items/5")
                                .header(
                                        "X-Guest-Cart-Token",
                                        guestToken.toString()
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0));

        verify(cartService).getOrCreateGuestCart(guestToken);
        verify(cartService).removeProduct(cart, 5L);
        verify(cartMapper).toResponse(updatedCart);
    }

    @Test
    void shouldClearGuestCart() throws Exception {

        UUID guestToken = UUID.randomUUID();

        Cart cart = new Cart(guestToken);
        Cart updatedCart = new Cart(guestToken);

        CartResponseDto response = new CartResponseDto(
                1L,
                guestToken,
                List.of(),
                0L,
                0L,
                0L
        );

        when(cartService.getOrCreateGuestCart(guestToken))
                .thenReturn(cart);

        when(cartService.clearCart(cart))
                .thenReturn(updatedCart);

        when(cartMapper.toResponse(updatedCart))
                .thenReturn(response);

        mockMvc.perform(
                        delete("/cart/items")
                                .header(
                                        "X-Guest-Cart-Token",
                                        guestToken.toString()
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.total").value(0));

        verify(cartService).getOrCreateGuestCart(guestToken);
        verify(cartService).clearCart(cart);
        verify(cartMapper).toResponse(updatedCart);
    }

    @Test
    void shouldUseUserCartWhenUserIsAuthenticated() throws Exception {

        String email = "cliente@test.com";

        Cart cart = new Cart(mock(com.tiendatcg.user.User.class));

        CartResponseDto response = new CartResponseDto(
                1L,
                null,
                List.of(),
                0L,
                0L,
                0L
        );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        List.of(
                                new SimpleGrantedAuthority("ROLE_CUSTOMER")
                        )
                );

        when(cartService.getOrCreateUserCart(email))
                .thenReturn(cart);

        when(cartMapper.toResponse(cart))
                .thenReturn(response);

        mockMvc.perform(
                        get("/cart")
                                .principal(authentication)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guestToken").doesNotExist())
                .andExpect(jsonPath("$.total").value(0));

        verify(cartService).getOrCreateUserCart(email);
        verify(cartService, never()).getOrCreateGuestCart(any());
        verify(cartMapper).toResponse(cart);
    }

    @Test
    void shouldReturn400WhenQuantityIsInvalid() throws Exception {

        UUID guestToken = UUID.randomUUID();

        mockMvc.perform(
                        post("/cart/items")
                                .header(
                                        "X-Guest-Cart-Token",
                                        guestToken.toString()
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                              "productId": 5,
                              "quantity": 0
                            }
                            """)
                )
                .andExpect(status().isBadRequest());

        verify(cartService, never()).addProduct(
                any(),
                any(),
                anyInt()
        );
    }
}
