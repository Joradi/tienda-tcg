package com.tiendatcg.order;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    public OrderController(OrderService orderService, OrderMapper orderMapper) {
        this.orderService = orderService;
        this.orderMapper = orderMapper;
    }

    @GetMapping
    public List<OrderResponseDto> getOrders(Authentication authentication)
    {
        return orderService.getUserOrders(authentication.getName())
                .stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @GetMapping("/{orderId}")
    public OrderResponseDto getOrder(Authentication authentication, @PathVariable Long orderId)
    {
        Order order = orderService.getUserOrder(authentication.getName(), orderId);
        return orderMapper.toResponse(order);
    }
}