package com.tiendatcg.order;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private OrderMapper orderMapper;

    @Test
    void getOrdersShouldReturnAuthenticatedUserOrders() throws Exception
    {

        String email = "customer@example.com";

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        List.of()
                );

        Order order1 = mock(Order.class);
        Order order2 = mock(Order.class);

        OrderResponseDto response1 = new OrderResponseDto(
                1L,
                email,
                "Customer User",
                "Av. Test 123",
                "PAID",
                8403L,
                1597L,
                10000L,
                LocalDateTime.now(),
                List.of()
        );

        OrderResponseDto response2 = new OrderResponseDto(
                2L,
                email,
                "Customer User",
                "Av. Test 123",
                "PAID",
                16807L,
                3193L,
                20000L,
                LocalDateTime.now(),
                List.of()
        );

        when(orderService.getUserOrders(email))
                .thenReturn(List.of(order1, order2));

        when(orderMapper.toResponse(order1))
                .thenReturn(response1);

        when(orderMapper.toResponse(order2))
                .thenReturn(response2);

        mockMvc.perform(get("/orders")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].orderId").value(1))
                .andExpect(jsonPath("$[0].total").value(10000))
                .andExpect(jsonPath("$[1].orderId").value(2))
                .andExpect(jsonPath("$[1].total").value(20000));

        verify(orderService).getUserOrders(email);

        verify(orderMapper).toResponse(order1);

        verify(orderMapper).toResponse(order2);
    }

    @Test
    void getOrderShouldReturnAuthenticatedUserOrder() throws Exception
    {

        String email = "customer@example.com";
        Long orderId = 15L;

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        List.of()
                );

        Order order = mock(Order.class);

        OrderResponseDto response = new OrderResponseDto(
                orderId,
                email,
                "Customer User",
                "Av. Test 123",
                "PAID",
                12605L,
                2395L,
                15000L,
                LocalDateTime.now(),
                List.of()
        );

        when(orderService.getUserOrder(email, orderId))
                .thenReturn(order);

        when(orderMapper.toResponse(order))
                .thenReturn(response);

        mockMvc.perform(get("/orders/{orderId}", orderId)
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(15))
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.total").value(15000));

        verify(orderService).getUserOrder(email, orderId);

        verify(orderMapper).toResponse(order);
    }
}