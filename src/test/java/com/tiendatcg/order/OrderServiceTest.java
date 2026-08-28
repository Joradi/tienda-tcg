package com.tiendatcg.order;

import com.tiendatcg.user.User;
import com.tiendatcg.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    private OrderService orderService;

    @BeforeEach
    void setUp()
    {
        orderService = new OrderService(
                orderRepository,
                userRepository
        );
    }

    @Test
    void getUserOrdersShouldReturnOrdersForUser()
    {
        String email = "customer@example.com";
        User user = mock(User.class);
        Order order1 = mock(Order.class);
        Order order2 = mock(Order.class);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(orderRepository.findByUserOrderByCreatedAtDesc(user))
                .thenReturn(List.of(order1, order2));

        List<Order> result = orderService.getUserOrders(email);

        assertEquals(2, result.size());
        assertSame(order1, result.get(0));
        assertSame(order2, result.get(1));

        verify(userRepository).findByEmail(email);

        verify(orderRepository).findByUserOrderByCreatedAtDesc(user);
    }

    @Test
    void getUserOrderShouldReturnOrderOwnedByUser()
    {
        String email = "customer@example.com";
        Long orderId = 10L;
        User user = mock(User.class);
        Order order = mock(Order.class);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(orderRepository.findByIdAndUser(orderId, user))
                .thenReturn(Optional.of(order));

        Order result = orderService.getUserOrder(email, orderId);

        assertSame(order, result);

        verify(userRepository).findByEmail(email);

        verify(orderRepository).findByIdAndUser(orderId, user);
    }

    @Test
    void getUserOrderShouldThrowWhenOrderDoesNotBelongToUser()
    {
        String email = "customer@example.com";
        Long orderId = 99L;
        User user = mock(User.class);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(orderRepository.findByIdAndUser(orderId, user))
                .thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class,
                () -> orderService.getUserOrder(email, orderId));

        verify(orderRepository).findByIdAndUser(orderId, user);
    }

    @Test
    void getUserOrderShouldThrowWhenUserDoesNotExist()
    {
        String email = "missing@example.com";

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> orderService.getUserOrder(email, 1L)
        );

        verifyNoInteractions(orderRepository);
    }
}