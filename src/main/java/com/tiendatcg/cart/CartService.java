package com.tiendatcg.cart;

import com.tiendatcg.product.Product;
import com.tiendatcg.product.ProductRepository;
import com.tiendatcg.user.User;
import com.tiendatcg.user.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository, UserRepository userRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public Cart getOrCreateUserCart(String email)
    {
        User user = userRepository.findByEmail(email).orElseThrow(() ->
                        new UsernameNotFoundException("Usuario no encontrado"));

        return cartRepository.findByUser(user).orElseGet(() ->
                        cartRepository.save(new Cart(user)));
    }

    @Transactional
    public Cart getOrCreateGuestCart(UUID guestToken)
    {
        if (guestToken == null) {
            return cartRepository.save(new Cart(UUID.randomUUID()));
        }

        return cartRepository.findByGuestToken(guestToken).orElseThrow(() ->
                        new CartNotFoundException("Carrito invitado no encontrado"));
    }

    @Transactional
    public Cart addProduct(Cart cart, Long productId, int quantity)
    {
        if (quantity <= 0)
        {
            throw new InvalidCartQuantityException("La cantidad debe ser mayor que cero");
        }

        Product product = productRepository.findById(productId).orElseThrow(() ->
                        new CartProductNotFoundException("Producto no encontrado"));

        CartItem existingItem = cart.getItems()
                .stream()
                .filter(item ->
                        item.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);

        int finalQuantity = quantity;

        if (existingItem != null)
        {
            finalQuantity += existingItem.getQuantity();
        }

        if (finalQuantity > product.getStock())
        {
            throw new InsufficientStockException("No hay stock suficiente");
        }

        if (existingItem != null)
        {
            existingItem.setQuantity(finalQuantity);
        }
        else {
            cart.getItems().add(new CartItem(cart, product, quantity));
        }
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart updateQuantity(Cart cart, Long productId, int quantity)
    {
        if (quantity <= 0)
        {
            throw new InvalidCartQuantityException("La cantidad debe ser mayor que cero");
        }

        CartItem item = cart.getItems()
                .stream()
                .filter(cartItem ->
                        cartItem.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() ->
                        new CartItemNotFoundException("Producto no encontrado en el carrito")
                );
        if (quantity > item.getProduct().getStock())
        {
            throw new InsufficientStockException("No hay stock suficiente");
        }
        item.setQuantity(quantity);
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart removeProduct(Cart cart, Long productId)
    {
        boolean removed = cart.getItems().removeIf(item ->
                        item.getProduct().getId().equals(productId));
        if (!removed)
        {
            throw new CartItemNotFoundException("Producto no encontrado en el carrito");
        }
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart clearCart(Cart cart)
    {
        cart.getItems().clear();
        return cartRepository.save(cart);
    }
}