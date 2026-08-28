package com.tiendatcg.order;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@WebMvcTest(CheckoutController.class)
@AutoConfigureMockMvc(addFilters = false)
class CheckoutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CheckoutService checkoutService;

    @MockitoBean
    private OrderMapper orderMapper;

    @Test
    void checkoutGuestShouldUseGuestToken() throws Exception
    {
        UUID guestToken = UUID.randomUUID();
        Order order = mock(Order.class);
        OrderResponseDto response = new OrderResponseDto(
                1L,
                "guest@example.com",
                "Guest User",
                "Av. Test 123",
                "PAID",
                8403L,
                1597L,
                10000L,
                LocalDateTime.now(),
                List.of()
        );

        when(checkoutService.checkoutGuest(
                eq(guestToken),
                any(CheckoutRequest.class)
        )).thenReturn(order);

        when(orderMapper.toResponse(order))
                .thenReturn(response);

        mockMvc.perform(post("/checkout")
                        .header("X-Guest-Cart-Token", guestToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "customerName": "Guest User",
                              "customerEmail": "guest@example.com",
                              "shippingAddress": "Av. Test 123"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.total").value(10000));

        verify(checkoutService).checkoutGuest(
                eq(guestToken),
                any(CheckoutRequest.class)
        );

        verify(checkoutService, never())
                .checkoutUser(anyString(), any());
    }

    @Test
    void checkoutAuthenticatedUserShouldUseUserCart() throws Exception
    {

        String email = "customer@example.com";
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(email, null, List.of());

        Order order = mock(Order.class);

        OrderResponseDto response = new OrderResponseDto(
                2L,
                email,
                "Customer User",
                "Av. Cliente 456",
                "PAID",
                16807L,
                3193L,
                20000L,
                LocalDateTime.now(),
                List.of()
        );

        when(checkoutService.checkoutUser(eq(email),
                any(CheckoutRequest.class))).thenReturn(order);

        when(orderMapper.toResponse(order))
                .thenReturn(response);

        mockMvc.perform(post("/checkout")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "customerName": "Customer User",
                              "customerEmail": "customer@example.com",
                              "shippingAddress": "Av. Cliente 456"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(2))
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.total").value(20000));

        verify(checkoutService).checkoutUser(eq(email),
                any(CheckoutRequest.class));

        verify(checkoutService, never()).checkoutGuest(any(), any());
    }

    @Test
    void checkoutWithInvalidRequestShouldReturnBadRequest() throws Exception
    {
        mockMvc.perform(post("/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "customerName": "",
                              "customerEmail": "not-an-email",
                              "shippingAddress": ""
                            }
                            """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(checkoutService);
        verifyNoInteractions(orderMapper);
    }

    @Test
    void checkoutWithInvalidRequestShouldReturnStandardErrorResponse() throws Exception
    {
        mockMvc.perform(post("/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "customerName": "",
                              "customerEmail": "not-an-email",
                              "shippingAddress": ""
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/checkout"))
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(checkoutService);
        verifyNoInteractions(orderMapper);
    }

    @Test
    void checkoutWithInsufficientStockShouldReturnConflictErrorResponse() throws Exception
    {
        UUID guestToken = UUID.randomUUID();
        when(checkoutService.checkoutGuest(eq(guestToken),
                any(CheckoutRequest.class))).thenThrow(
                new CheckoutStockException(
                        "Stock insuficiente para el producto 10"
                )
        );

        mockMvc.perform(post("/checkout")
                        .header("X-Guest-Cart-Token", guestToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "customerName": "Guest User",
                              "customerEmail": "guest@example.com",
                              "shippingAddress": "Av. Test 123"
                            }
                            """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value("Stock insuficiente para el producto 10"))
                .andExpect(jsonPath("$.path").value("/checkout"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(orderMapper, never())
                .toResponse(any(Order.class));
    }
}