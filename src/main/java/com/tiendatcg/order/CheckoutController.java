package com.tiendatcg.order;

import jakarta.validation.Valid;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/checkout")
public class CheckoutController {

    private final CheckoutService checkoutService;
    private final OrderMapper orderMapper;

    public CheckoutController(CheckoutService checkoutService, OrderMapper orderMapper) {
        this.checkoutService = checkoutService;
        this.orderMapper = orderMapper;
    }

    @PostMapping
    public OrderResponseDto checkout(Authentication authentication, @RequestHeader(value = "X-Guest-Cart-Token", required = false) UUID guestToken,
            @Valid @RequestBody CheckoutRequest request)
    {
        boolean authenticatedUser = authentication != null
                        && authentication.isAuthenticated()
                        && !(authentication instanceof AnonymousAuthenticationToken);
        Order order;
        if (authenticatedUser) {
            order = checkoutService.checkoutUser(authentication.getName(), request);
        }
        else {
            order = checkoutService.checkoutGuest(guestToken, request);
        }

        return orderMapper.toResponse(order);
    }
}