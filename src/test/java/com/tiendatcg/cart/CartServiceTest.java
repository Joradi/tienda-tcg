package com.tiendatcg.cart;

import com.tiendatcg.card.Card;
import com.tiendatcg.product.*;
import com.tiendatcg.user.User;
import com.tiendatcg.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    @Test
    void shouldAddNewProductToCart()
    {
        Cart cart = new Cart(UUID.randomUUID());

        Card card = new Card();

        Product product  = new Product(
                card,
                Language.ENGLISH,
                Variant.NORMAL,
                Condition.NEAR_MINT,
                5,
                15000L,
                LocalDate.now()
        );

        when(productRepository.findById(5L))
                .thenReturn(Optional.of(product));

        when(cartRepository.save(cart))
                .thenReturn(cart);

        Cart result = cartService.addProduct(
                cart,
                5L,
                2
        );

        assertEquals(1, result.getItems().size());
        assertEquals(2, result.getItems().getFirst().getQuantity());
        assertSame(product, result.getItems().getFirst().getProduct());

        verify(cartRepository).save(cart);
    }

    @Test
    void shouldIncreaseQuantityWhenProductAlreadyExistsInCart()
    {
        Cart cart = new Cart(UUID.randomUUID());

        Product product = mock(Product.class);

        when(product.getId()).thenReturn(5L);
        when(product.getStock()).thenReturn(10);

        cart.getItems().add(
                new CartItem(cart, product, 2)
        );

        when(productRepository.findById(5L))
                .thenReturn(Optional.of(product));

        when(cartRepository.save(cart))
                .thenReturn(cart);

        Cart result = cartService.addProduct(
                cart,
                5L,
                3
        );

        assertEquals(1, result.getItems().size());
        assertEquals(5, result.getItems().getFirst().getQuantity());

        verify(cartRepository).save(cart);
    }

    @Test
    void shouldThrowWhenFinalQuantityExceedsStock()
    {
        Cart cart = new Cart(UUID.randomUUID());
        Product product = mock(Product.class);

        when(product.getId()).thenReturn(5L);
        when(product.getStock()).thenReturn(4);

        cart.getItems().add(
                new CartItem(cart, product, 2)
        );

        when(productRepository.findById(5L))
                .thenReturn(Optional.of(product));

        assertThrows(
                InsufficientStockException.class,
                () -> cartService.addProduct(
                        cart,
                        5L,
                        3
                )
        );

        verify(cartRepository, never()).save(cart);
    }

    @Test
    void shouldUpdateItemQuantity() {

        Cart cart = new Cart(UUID.randomUUID());

        Product product = mock(Product.class);

        when(product.getId()).thenReturn(5L);
        when(product.getStock()).thenReturn(10);

        CartItem item = new CartItem(cart, product, 2);
        cart.getItems().add(item);

        when(cartRepository.save(cart))
                .thenReturn(cart);

        Cart result = cartService.updateQuantity(
                cart,
                5L,
                6
        );

        assertEquals(6, result.getItems().getFirst().getQuantity());

        verify(cartRepository).save(cart);
    }

    @Test
    void shouldThrowWhenUpdatedQuantityExceedsStock()
    {
        Cart cart = new Cart(UUID.randomUUID());

        Product product = mock(Product.class);

        when(product.getId()).thenReturn(5L);
        when(product.getStock()).thenReturn(4);

        cart.getItems().add(
                new CartItem(cart, product, 2)
        );

        assertThrows(
                InsufficientStockException.class,
                () -> cartService.updateQuantity(
                        cart,
                        5L,
                        6
                )
        );

        verify(cartRepository, never()).save(cart);
    }

    @Test
    void shouldRemoveProductFromCart()
    {
        Cart cart = new Cart(UUID.randomUUID());

        Product product = mock(Product.class);

        when(product.getId()).thenReturn(5L);

        cart.getItems().add(
                new CartItem(cart, product, 2)
        );

        when(cartRepository.save(cart))
                .thenReturn(cart);

        Cart result = cartService.removeProduct(
                cart,
                5L
        );

        assertEquals(0, result.getItems().size());

        verify(cartRepository).save(cart);
    }

    @Test
    void shouldThrowWhenRemovingMissingProduct()
    {
        Cart cart = new Cart(UUID.randomUUID());

        assertThrows(
                CartItemNotFoundException.class,
                () -> cartService.removeProduct(
                        cart,
                        5L
                )
        );

        verify(cartRepository, never()).save(cart);
    }

    @Test
    void shouldClearCart()
    {
        Cart cart = new Cart(UUID.randomUUID());

        Product product1 = mock(Product.class);
        Product product2 = mock(Product.class);

        cart.getItems().add(
                new CartItem(cart, product1, 2)
        );

        cart.getItems().add(
                new CartItem(cart, product2, 1)
        );

        when(cartRepository.save(cart))
                .thenReturn(cart);

        Cart result = cartService.clearCart(cart);

        assertEquals(0, result.getItems().size());

        verify(cartRepository).save(cart);

    }

    @Test
    void shouldCreateGuestCartWhenTokenIsNull() {

        when(cartRepository.save(any(Cart.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Cart result = cartService.getOrCreateGuestCart(null);

        assertNotNull(result.getGuestToken());

        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void shouldReturnExistingGuestCartWhenTokenExists()
    {
        UUID guestToken = UUID.randomUUID();
        Cart existingCart = new Cart(guestToken);

        when(cartRepository.findByGuestToken(guestToken))
                .thenReturn(Optional.of(existingCart));

        Cart result = cartService.getOrCreateGuestCart(guestToken);

        assertSame(existingCart, result);

        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void shouldThrowWhenGuestTokenDoesNotExist() {

        UUID guestToken = UUID.randomUUID();

        when(cartRepository.findByGuestToken(guestToken))
                .thenReturn(Optional.empty());

        assertThrows(
                CartNotFoundException.class,
                () -> cartService.getOrCreateGuestCart(guestToken)
        );

        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void shouldReturnExistingUserCart()
    {
        String email = "cliente@test.com";

        User user = mock(User.class);
        Cart existingCart = new Cart(user);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUser(user))
                .thenReturn(Optional.of(existingCart));

        Cart result = cartService.getOrCreateUserCart(email);

        assertSame(existingCart, result);

        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void shouldCreateCartForUserWhenNoneExists() {

        String email = "cliente@test.com";

        User user = mock(User.class);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUser(user))
                .thenReturn(Optional.empty());

        when(cartRepository.save(any(Cart.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Cart result = cartService.getOrCreateUserCart(email);

        assertSame(user, result.getUser());

        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void shouldThrowWhenUserDoesNotExist()
    {
        String email = "noexiste@test.com";

        assertThrows(
                UsernameNotFoundException.class,
                () -> cartService.getOrCreateUserCart(email)
        );

        verify(cartRepository, never()).findByUser(any(User.class));
        verify(cartRepository, never()).save(any(Cart.class));
    }
}