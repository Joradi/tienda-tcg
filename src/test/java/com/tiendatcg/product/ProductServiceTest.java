package com.tiendatcg.product;

import com.tiendatcg.card.Card;
import com.tiendatcg.card.CardService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CardService cardService;

    @InjectMocks
    private ProductService productService;

    @Test
    void shouldThrowWhenProductDoesNoExist()
    {
        when(productRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ProductNotFoundException.class,
                () -> productService.getProductById(999L)
        );
    }

    @Test
    void shouldThrowWhenProductAlreadyExists()
    {
        Card card = new Card();

        ProductCreateRequest request = mock(ProductCreateRequest.class);

        when(request.getCardId()).thenReturn(5L);
        when(request.getLanguage()).thenReturn(Language.ENGLISH);
        when(request.getVariant()).thenReturn(Variant.HOLO);
        when(request.getCondition()).thenReturn(Condition.NEAR_MINT);
        when(request.getStock()).thenReturn(2);
        when(request.getPrice()).thenReturn(45000L);

        when(cardService.findById(5L))
                .thenReturn(Optional.of(card));

        when(productRepository.findByCardAndLanguageAndVariantAndCondition(
                card,
                Language.ENGLISH,
                Variant.HOLO,
                Condition.NEAR_MINT
        )).thenReturn(Optional.of(new Product()));

        assertThrows(
                ProductAlreadyExistsException.class,
                () -> productService.createProduct(request)
        );

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void shouldUpdateStockWithoutChangingPrice()
    {
        Product product = new Product(
                new Card(),
                Language.ENGLISH,
                Variant.NORMAL,
                Condition.NEAR_MINT,
                3,
                12000L,
                LocalDate.now()
        );

        ProductUpdateRequest request = mock(ProductUpdateRequest.class);

        when(request.getStock()).thenReturn(10);
        when(request.getPrice()).thenReturn(null);

        when(productRepository.findById(2L))
                .thenReturn(Optional.of(product));

        when(productRepository.save(product))
                .thenReturn(product);

        Product result = productService.updateProduct(2L, request);

        assertEquals(10, result.getStock());
        assertEquals(12000L, result.getPrice());

        verify(productRepository).save(product);
    }
}
