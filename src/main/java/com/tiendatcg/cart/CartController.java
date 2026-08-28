package com.tiendatcg.cart;

import jakarta.validation.Valid;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final CartMapper cartMapper;

    public CartController(
            CartService cartService,
            CartMapper cartMapper
    ) {
        this.cartService = cartService;
        this.cartMapper = cartMapper;
    }

    @GetMapping
    public CartResponseDto getCart(Authentication authentication, @RequestHeader(value = "X-Guest-Cart-Token",
            required = false) UUID guestToken)
    {
        Cart cart = resolveCart(authentication, guestToken);
        return cartMapper.toResponse(cart);
    }

    @PostMapping("/items")
    public CartResponseDto addItem(Authentication authentication, @RequestHeader(value = "X-Guest-Cart-Token", required = false) UUID guestToken,
            @Valid @RequestBody CartAddItemRequest request)
    {
        Cart cart = resolveCart(authentication, guestToken);
        Cart updatedCart = cartService.addProduct(cart, request.getProductId(), request.getQuantity());
        return cartMapper.toResponse(updatedCart);
    }

    @PatchMapping("/items/{productId}")
    public CartResponseDto updateItemQuantity(Authentication authentication, @RequestHeader(value = "X-Guest-Cart-Token", required = false) UUID guestToken,
            @PathVariable Long productId, @Valid @RequestBody CartUpdateItemRequest request)
    {
        Cart cart = resolveCart(authentication, guestToken);
        Cart updatedCart = cartService.updateQuantity(cart, productId, request.getQuantity());
        return cartMapper.toResponse(updatedCart);
    }

    @DeleteMapping("/items/{productId}")
    public CartResponseDto removeItem(Authentication authentication, @RequestHeader(value = "X-Guest-Cart-Token", required = false) UUID guestToken,
            @PathVariable Long productId)
    {
        Cart cart = resolveCart(authentication, guestToken);
        Cart updatedCart = cartService.removeProduct(cart, productId);
        return cartMapper.toResponse(updatedCart);
    }

    @DeleteMapping("/items")
    public CartResponseDto clearCart(Authentication authentication, @RequestHeader(value = "X-Guest-Cart-Token", required = false) UUID guestToken)
    {
        Cart cart = resolveCart(authentication, guestToken);
        Cart updatedCart = cartService.clearCart(cart);
        return cartMapper.toResponse(updatedCart);
    }

    private Cart resolveCart(Authentication authentication, UUID guestToken)
    {
        boolean authenticatedUser = authentication != null
                        && authentication.isAuthenticated()
                        && !(authentication instanceof AnonymousAuthenticationToken);

        if (authenticatedUser)
        {
            return cartService.getOrCreateUserCart(authentication.getName());
        }
        return cartService.getOrCreateGuestCart(guestToken);
    }
}