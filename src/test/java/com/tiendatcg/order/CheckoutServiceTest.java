package com.tiendatcg.order;

import com.tiendatcg.cart.Cart;
import com.tiendatcg.cart.CartItem;
import com.tiendatcg.product.Language;
import com.tiendatcg.product.Variant;
import com.tiendatcg.product.Condition;
import com.tiendatcg.cart.CartNotFoundException;
import com.tiendatcg.cart.CartRepository;
import com.tiendatcg.pricing.TaxBreakdown;
import com.tiendatcg.pricing.TaxCalculator;
import com.tiendatcg.product.Product;
import com.tiendatcg.product.ProductRepository;
import com.tiendatcg.user.User;
import com.tiendatcg.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import org.mockito.InOrder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaxCalculator taxCalculator;

    private CheckoutService checkoutService;

    @BeforeEach
    void setUp()
    {
        checkoutService = new CheckoutService(orderRepository,
                productRepository,
                cartRepository,
                userRepository,
                taxCalculator
        );
    }

    @Test
    void checkoutGuestWithoutTokenShouldThrowCartNotFoundException()
    {
        CheckoutRequest request = new CheckoutRequest();

        assertThrows(CartNotFoundException.class, () -> checkoutService.checkoutGuest(null, request)
        );

        verifyNoInteractions(
                orderRepository,
                productRepository,
                cartRepository,
                userRepository,
                taxCalculator
        );
    }

    @Test
    void checkoutGuestWithEmptyCartShouldThrowEmptyCartException()
    {
        UUID guestToken = UUID.randomUUID();
        Cart cart = new Cart(guestToken);
        CheckoutRequest request = new CheckoutRequest();

        when(cartRepository.findByGuestTokenForUpdate(guestToken))
                .thenReturn(Optional.of(cart));

        assertThrows(
                EmptyCartException.class, () -> checkoutService.checkoutGuest(guestToken, request));

        verify(cartRepository).findByGuestTokenForUpdate(guestToken);

        verifyNoInteractions(orderRepository, productRepository, taxCalculator);
    }

    @Test
    void checkoutGuestWithInsufficientStockShouldThrowCheckoutStockException()
    {
        UUID guestToken = UUID.randomUUID();
        Product cartProduct = mock(Product.class);
        Product lockedProduct = mock(Product.class);

        when(cartProduct.getId()).thenReturn(10L);

        when(lockedProduct.getId()).thenReturn(10L);
        when(lockedProduct.getStock()).thenReturn(1);

        Cart cart = new Cart(guestToken);
        cart.getItems().add(new CartItem(cart, cartProduct, 2));

        CheckoutRequest request = new CheckoutRequest();

        when(cartRepository.findByGuestTokenForUpdate(guestToken))
                .thenReturn(Optional.of(cart));

        when(productRepository.findByIdForUpdate(10L))
                .thenReturn(Optional.of(lockedProduct));

        assertThrows(CheckoutStockException.class,
                () -> checkoutService.checkoutGuest(guestToken, request));

        verify(productRepository).findByIdForUpdate(10L);

        verify(orderRepository, never()).save(any(Order.class));

        verify(taxCalculator, never()).calculateFromTaxIncludedTotal(anyLong());
    }

    @Test
    void checkoutGuestShouldCreateOrderDecreaseStockAndClearCart()
    {
        UUID guestToken = UUID.randomUUID();
        Product product = mock(Product.class, RETURNS_DEEP_STUBS);

        when(product.getId()).thenReturn(10L);
        when(product.getStock()).thenReturn(5);
        when(product.getPrice()).thenReturn(5000L);

        when(product.getCard().getName()).thenReturn("Test Card");

        when(product.getLanguage()).thenReturn(Language.values()[0]);

        when(product.getVariant()).thenReturn(Variant.values()[0]);

        when(product.getCondition()).thenReturn(Condition.values()[0]);

        Cart cart = new Cart(guestToken);

        cart.getItems().add(
                new CartItem(
                        cart,
                        product,
                        2
                )
        );

        CheckoutRequest request = new CheckoutRequest();
        request.setCustomerName("Guest User");
        request.setCustomerEmail("guest@example.com");
        request.setShippingAddress("Av. Test 123");

        when(cartRepository.findByGuestTokenForUpdate(guestToken))
                .thenReturn(Optional.of(cart));

        when(productRepository.findByIdForUpdate(10L))
                .thenReturn(Optional.of(product));

        when(taxCalculator.calculateFromTaxIncludedTotal(10000L))
                .thenReturn(
                        new TaxBreakdown(
                                8403L,
                                1597L,
                                10000L
                        )
                );

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Order result = checkoutService.checkoutGuest(guestToken, request);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);

        verify(orderRepository).save(orderCaptor.capture());

        Order savedOrder = orderCaptor.getValue();

        assertAll(() -> assertSame(savedOrder, result),
                () -> assertEquals(
                        "Guest User",
                        savedOrder.getCustomerName()
                ), () -> assertEquals(
                        "guest@example.com",
                        savedOrder.getCustomerEmail()
                ), () -> assertEquals("Av. Test 123",
                        savedOrder.getShippingAddress()
                ), () -> assertEquals(
                        OrderStatus.PAID,
                        savedOrder.getStatus()
                ), () -> assertEquals(
                        8403L,
                        savedOrder.getNetAmount()
                ), () -> assertEquals(
                        1597L,
                        savedOrder.getTaxAmount()
                ), () -> assertEquals(
                        10000L,
                        savedOrder.getTotal()
                ), () -> assertNull(
                        savedOrder.getUser()
                ), () -> assertEquals(
                        1,
                        savedOrder.getItems().size()
                ), () -> assertEquals(
                        10L,
                        savedOrder.getItems().get(0).getProductId()
                ), () -> assertEquals(
                        "Test Card",
                        savedOrder.getItems().get(0).getCardName()
                ), () -> assertEquals(
                        5000L,
                        savedOrder.getItems().get(0).getUnitPrice()
                ), () -> assertEquals(
                        2,
                        savedOrder.getItems().get(0).getQuantity()
                ), () -> assertEquals(
                        10000L,
                        savedOrder.getItems().get(0).getSubtotal()
                ), () -> assertTrue(
                        cart.getItems().isEmpty()
                ));

        verify(product).setStock(3);

        verify(productRepository).findByIdForUpdate(10L);

        verify(taxCalculator).calculateFromTaxIncludedTotal(10000L);

        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void checkoutUserShouldCreateOrderAssociatedWithUser()
    {
        String email = "customer@example.com";
        User user = mock(User.class);
        Product product = mock(Product.class, RETURNS_DEEP_STUBS);

        when(product.getId()).thenReturn(20L);
        when(product.getStock()).thenReturn(4);
        when(product.getPrice()).thenReturn(12000L);

        when(product.getCard().getName()).thenReturn("User Test Card");

        when(product.getLanguage()).thenReturn(Language.values()[0]);

        when(product.getVariant()).thenReturn(Variant.values()[0]);

        when(product.getCondition()).thenReturn(Condition.values()[0]);

        Cart cart = new Cart(user);

        cart.getItems().add(new CartItem(cart, product, 1));

        CheckoutRequest request = new CheckoutRequest();
        request.setCustomerName("Customer User");
        request.setCustomerEmail(email);
        request.setShippingAddress("Av. Cliente 456");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        when(cartRepository.findByUserForUpdate(user)).thenReturn(Optional.of(cart));

        when(productRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(product));

        when(taxCalculator.calculateFromTaxIncludedTotal(12000L)).thenReturn(
                        new TaxBreakdown(
                                10084L,
                                1916L,
                                12000L
                        )
                );

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Order result = checkoutService.checkoutUser(email, request);

        assertAll(() -> assertSame(user, result.getUser()),
                () -> assertEquals(email, result.getCustomerEmail()),
                () -> assertEquals("Customer User", result.getCustomerName()),
                () -> assertEquals("Av. Cliente 456", result.getShippingAddress()),
                () -> assertEquals(OrderStatus.PAID, result.getStatus()),
                () -> assertEquals(12000L, result.getTotal()),
                () -> assertEquals(1, result.getItems().size()),
                () -> assertEquals("User Test Card", result.getItems().get(0).getCardName()),
                () -> assertTrue(cart.getItems().isEmpty())
        );

        verify(userRepository).findByEmail(email);

        verify(cartRepository).findByUserForUpdate(user);

        verify(productRepository).findByIdForUpdate(20L);

        verify(product).setStock(3);

        verify(orderRepository).save(any(Order.class));

        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void checkoutShouldUseCurrentLockedProductPrice()
    {
        UUID guestToken = UUID.randomUUID();
        Product cartProduct = mock(Product.class);
        Product lockedProduct = mock(Product.class, RETURNS_DEEP_STUBS);

        when(cartProduct.getId()).thenReturn(30L);

        when(lockedProduct.getId()).thenReturn(30L);

        when(lockedProduct.getStock()).thenReturn(5);

        when(lockedProduct.getPrice()).thenReturn(10000L);

        when(lockedProduct.getCard().getName()).thenReturn("Current Price Card");

        when(lockedProduct.getLanguage()).thenReturn(Language.values()[0]);

        when(lockedProduct.getVariant()).thenReturn(Variant.values()[0]);

        when(lockedProduct.getCondition()).thenReturn(Condition.values()[0]);

        Cart cart = new Cart(guestToken);

        cart.getItems().add(new CartItem(cart, cartProduct, 2));

        CheckoutRequest request = new CheckoutRequest();
        request.setCustomerName("Guest User");
        request.setCustomerEmail("guest@example.com");
        request.setShippingAddress("Av. Test 123");

        when(cartRepository.findByGuestTokenForUpdate(guestToken))
                .thenReturn(Optional.of(cart));

        when(productRepository.findByIdForUpdate(30L))
                .thenReturn(Optional.of(lockedProduct));

        when(taxCalculator.calculateFromTaxIncludedTotal(20000L))
                .thenReturn(new TaxBreakdown(
                                16807L,
                                3193L,
                                20000L
                        )
                );

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Order result = checkoutService.checkoutGuest(guestToken, request);

        OrderItem orderItem = result.getItems().get(0);

        assertAll(() -> assertEquals(20000L, result.getTotal()),
                () -> assertEquals(
                        10000L,
                        orderItem.getUnitPrice()
                ),
                () -> assertEquals(
                        2,
                        orderItem.getQuantity()
                ),
                () -> assertEquals(
                        20000L,
                        orderItem.getSubtotal()
                ),
                () -> assertEquals(
                        "Current Price Card",
                        orderItem.getCardName()
                )
        );

        verify(productRepository).findByIdForUpdate(30L);

        verify(taxCalculator).calculateFromTaxIncludedTotal(20000L);

        verify(lockedProduct).setStock(3);

        verify(cartProduct, never()).getPrice();
    }

    @Test
    void checkoutShouldLockProductsInAscendingIdOrder()
    {
        UUID guestToken = UUID.randomUUID();
        Product cartProduct20 = mock(Product.class);

        Product cartProduct10 = mock(Product.class);

        when(cartProduct20.getId()).thenReturn(20L);

        when(cartProduct10.getId()).thenReturn(10L);

        Product lockedProduct10 = mock(Product.class, RETURNS_DEEP_STUBS);

        Product lockedProduct20 = mock(Product.class, RETURNS_DEEP_STUBS);

        when(lockedProduct10.getId()).thenReturn(10L);

        when(lockedProduct10.getStock()).thenReturn(5);

        when(lockedProduct10.getPrice()).thenReturn(5000L);

        when(lockedProduct10.getCard().getName()).thenReturn("Card 10");

        when(lockedProduct10.getLanguage()).thenReturn(Language.values()[0]);

        when(lockedProduct10.getVariant()).thenReturn(Variant.values()[0]);

        when(lockedProduct10.getCondition()).thenReturn(Condition.values()[0]);

        when(lockedProduct20.getId()).thenReturn(20L);

        when(lockedProduct20.getStock()).thenReturn(5);

        when(lockedProduct20.getPrice()).thenReturn(5000L);

        when(lockedProduct20.getCard().getName()).thenReturn("Card 20");

        when(lockedProduct20.getLanguage()).thenReturn(Language.values()[0]);

        when(lockedProduct20.getVariant()).thenReturn(Variant.values()[0]);

        when(lockedProduct20.getCondition()).thenReturn(Condition.values()[0]);

        Cart cart = new Cart(guestToken);

        cart.getItems().add(
                new CartItem(cart, cartProduct20, 1));

        cart.getItems().add(new CartItem(cart, cartProduct10, 1));

        CheckoutRequest request = new CheckoutRequest();

        request.setCustomerName("Guest User");
        request.setCustomerEmail("guest@example.com");
        request.setShippingAddress("Av. Test 123");

        when(cartRepository.findByGuestTokenForUpdate(guestToken))
                .thenReturn(Optional.of(cart));

        when(productRepository.findByIdForUpdate(10L))
                .thenReturn(Optional.of(lockedProduct10));

        when(productRepository.findByIdForUpdate(20L))
                .thenReturn(Optional.of(lockedProduct20));

        when(taxCalculator.calculateFromTaxIncludedTotal(10000L))
                .thenReturn(
                        new TaxBreakdown(
                                8403L,
                                1597L,
                                10000L
                        )
                );

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        checkoutService.checkoutGuest(guestToken, request);

        InOrder inOrder = inOrder(productRepository);

        inOrder.verify(productRepository).findByIdForUpdate(10L);

        inOrder.verify(productRepository).findByIdForUpdate(20L);
    }
}