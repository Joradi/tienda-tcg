package com.tiendatcg.order;

import com.tiendatcg.cart.Cart;
import com.tiendatcg.cart.CartItem;
import com.tiendatcg.cart.CartNotFoundException;
import com.tiendatcg.cart.CartRepository;
import com.tiendatcg.pricing.TaxBreakdown;
import com.tiendatcg.pricing.TaxCalculator;
import com.tiendatcg.product.Product;
import com.tiendatcg.product.ProductRepository;
import com.tiendatcg.user.User;
import com.tiendatcg.user.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class CheckoutService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final TaxCalculator taxCalculator;

    public CheckoutService(OrderRepository orderRepository, ProductRepository productRepository, CartRepository cartRepository, UserRepository userRepository,
                           TaxCalculator taxCalculator) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.taxCalculator = taxCalculator;
    }

    @Transactional
    public Order checkoutUser(String email, CheckoutRequest request)
    {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        Cart cart = cartRepository.findByUserForUpdate(user)
                .orElseThrow(() -> new CartNotFoundException("Carrito del usuario no encontrado"));

        return processCheckout(cart, request);
    }

    @Transactional
    public Order checkoutGuest(UUID guestToken, CheckoutRequest request)
    {
        if (guestToken == null)
        {
            throw new CartNotFoundException("Se requiere un token de carrito invitado");
        }

        Cart cart = cartRepository.findByGuestTokenForUpdate(guestToken)
                .orElseThrow(() -> new CartNotFoundException("Carrito invitado no encontrado"));

        return processCheckout(cart, request);
    }

    private Order processCheckout(Cart cart, CheckoutRequest request)
    {
        if (cart.getItems().isEmpty())
        {
            throw new EmptyCartException("No se puede realizar checkout con un carrito vacío");
        }

        List<CartItem> items = cart.getItems()
                .stream()
                .sorted(
                        Comparator.comparing(
                                item -> item.getProduct().getId()))
                .toList();

        List<Product> lockedProducts = new ArrayList<>();

        long total = 0L;

        for (CartItem item : items)
        {
            Product product = productRepository
                    .findByIdForUpdate(item.getProduct().getId())
                    .orElseThrow(() ->
                            new CheckoutStockException("Uno de los productos ya no está disponible"));

            if (product.getStock() < item.getQuantity())
            {
                throw new CheckoutStockException("Stock insuficiente para el producto " + product.getId());
            }

            product.setStock(product.getStock() - item.getQuantity());

            lockedProducts.add(product);

            total += product.getPrice() * item.getQuantity();
        }

        TaxBreakdown taxBreakdown = taxCalculator.calculateFromTaxIncludedTotal(total);

        Order order = new Order(cart.getUser(),
                request.getCustomerEmail(),
                request.getCustomerName(),
                request.getShippingAddress(),
                OrderStatus.PAID,
                taxBreakdown.netAmount(),
                taxBreakdown.taxAmount(),
                taxBreakdown.total(),
                LocalDateTime.now()
        );

        for (int i = 0; i < items.size(); i++)
        {
            CartItem cartItem = items.get(i);
            Product product = lockedProducts.get(i);
            long subtotal = product.getPrice() * cartItem.getQuantity();

            OrderItem orderItem = new OrderItem(order, product.getId(),
                    product.getCard().getName(),
                    product.getCard().getImageUrl(),
                    product.getLanguage().name(),
                    product.getVariant().name(),
                    product.getCondition().name(),
                    product.getPrice(),
                    cartItem.getQuantity(),
                    subtotal
            );
            order.getItems().add(orderItem);
        }

        Order savedOrder = orderRepository.save(order);
        cart.getItems().clear();

        return savedOrder;
    }
}