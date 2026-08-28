package com.tiendatcg.order;

import com.tiendatcg.card.Card;
import com.tiendatcg.cart.Cart;
import com.tiendatcg.cart.CartItem;
import com.tiendatcg.product.Condition;
import com.tiendatcg.product.Language;
import com.tiendatcg.product.Product;
import com.tiendatcg.product.ProductRepository;
import com.tiendatcg.product.Variant;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CheckoutConcurrencyIntegrationTest {

    @Autowired
    private CheckoutService checkoutService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp()
    {
        transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Test
    void concurrentGuestCheckoutsShouldNotOversellLastUnit() throws Exception
    {
        UUID guestTokenA = UUID.randomUUID();
        UUID guestTokenB = UUID.randomUUID();
        String suffix = UUID.randomUUID().toString();
        SetupData data = createTestData(guestTokenA, guestTokenB, suffix);

        CheckoutRequest requestA = createRequest(
                "Buyer A",
                "buyer-a-" + suffix + "@example.com"
        );

        CheckoutRequest requestB = createRequest(
                "Buyer B",
                "buyer-b-" + suffix + "@example.com"
        );

        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        CyclicBarrier barrier = new CyclicBarrier(2);

        List<Order> successfulOrders = new ArrayList<>();

        try {
            Future<Order> futureA = executor.submit(() -> {
                barrier.await();

                return checkoutService.checkoutGuest(
                        guestTokenA,
                        requestA
                );
            });

            Future<Order> futureB = executor.submit(() -> {
                barrier.await();
                return checkoutService.checkoutGuest(
                        guestTokenB,
                        requestB
                );
            });

            int stockFailures = 0;

            for (Future<Order> future : List.of(futureA, futureB)) {

                try {
                    Order order =
                            future.get(
                                    10,
                                    TimeUnit.SECONDS
                            );

                    successfulOrders.add(order);

                } catch (ExecutionException exception) {

                    Throwable cause = exception.getCause();

                    if (cause instanceof CheckoutStockException)
                    {
                        stockFailures++;
                    }
                    else {
                        throw new AssertionError("El checkout falló por una causa inesperada", cause);
                    }
                }
            }

            assertEquals(1, successfulOrders.size(), "Solo un checkout debe completarse");

            assertEquals(1, stockFailures, "El segundo checkout debe fallar por stock");

            assertEquals(OrderStatus.PAID, successfulOrders.get(0).getStatus());

            Product finalProduct = productRepository.findById(data.productId()).orElseThrow();

            assertEquals(0, finalProduct.getStock(), "El stock final debe quedar exactamente en cero");

        }
        finally {
            executor.shutdownNow();

            executor.awaitTermination(5, TimeUnit.SECONDS);

            cleanup(data, successfulOrders);
        }
    }

    private SetupData createTestData(UUID guestTokenA, UUID guestTokenB, String suffix)
    {

        SetupData data = transactionTemplate.execute(status -> {
            Card card = new Card(
                    "concurrency-test-" + suffix,
                    "Concurrency Test Card",
                    "Pokemon",
                    List.of("Test"),
                    "Integration Test",
                    "1",
                    "Common",
                    null,
                    null
            );

            entityManager.persist(card);

            Product product = new Product(
                    card,
                    Language.values()[0],
                    Variant.values()[0],
                    Condition.values()[0],
                    1,
                    10000L,
                    LocalDate.now()
            );

            entityManager.persist(product);

            Cart cartA = new Cart(guestTokenA);

            cartA.getItems().add(
                    new CartItem(
                            cartA,
                            product,
                            1
                    ));

            entityManager.persist(cartA);

            Cart cartB = new Cart(guestTokenB);

            cartB.getItems().add(
                    new CartItem(
                            cartB,
                            product,
                            1
                    )
            );

            entityManager.persist(cartB);

            entityManager.flush();

            return new SetupData(
                    card.getId(),
                    product.getId(),
                    cartA.getId(),
                    cartB.getId()
            );
        });

        if (data == null)
        {
            throw new IllegalStateException("No se pudieron crear los datos del test");
        }

        return data;
    }

    private CheckoutRequest createRequest(String name, String email)
    {
        CheckoutRequest request = new CheckoutRequest();
        request.setCustomerName(name);
        request.setCustomerEmail(email);
        request.setShippingAddress("Integration Test Address");

        return request;
    }

    private void cleanup(SetupData data, List<Order> successfulOrders)
    {
        transactionTemplate.executeWithoutResult(status -> {
            for (Order order : successfulOrders)
            {
                if (order.getId() != null)
                {
                    Order managedOrder = entityManager.find(Order.class, order.getId());

                    if (managedOrder != null)
                    {
                        entityManager.remove(managedOrder);
                    }
                }
            }

            Cart cartA = entityManager.find(Cart.class, data.cartAId());

            if (cartA != null)
            {
                entityManager.remove(cartA);
            }

            Cart cartB = entityManager.find(Cart.class, data.cartBId());

            if (cartB != null)
            {
                entityManager.remove(cartB);
            }

            Product product = entityManager.find(Product.class, data.productId());

            if (product != null)
            {
                entityManager.remove(product);
            }

            Card card = entityManager.find(Card.class, data.cardId());

            if (card != null)
            {
                entityManager.remove(card);
            }
        });
    }

    private record SetupData(
            Long cardId,
            Long productId,
            Long cartAId,
            Long cartBId
    ) {
    }
}