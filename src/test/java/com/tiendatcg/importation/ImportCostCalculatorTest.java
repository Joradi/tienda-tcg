package com.tiendatcg.importation;

import com.tiendatcg.card.Card;
import com.tiendatcg.product.Condition;
import com.tiendatcg.product.Language;
import com.tiendatcg.product.Variant;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ImportCostCalculatorTest {

    private final ImportCostCalculator calculator =
            new ImportCostCalculator();

    @Test
    void shouldCalculateBaseImportationCosts()
    {
        Importation importation = new Importation(
                        ImportOrigin.USA,
                        40000L,
                        80000L,
                        10000L,
                        10000L,
                        new BigDecimal("450.00")
                );

        Card charizard = new Card();
        charizard.setName("Charizard");

        Card pikachu = new Card();
        pikachu.setName("Pikachu");

        ImportItem firstItem = new ImportItem(
                        charizard,
                        Language.ENGLISH,
                        Variant.NORMAL,
                        Condition.NEAR_MINT,
                        2,
                        30000L,
                        50000L
                );

        ImportItem secondItem = new ImportItem(
                        pikachu,
                        Language.ENGLISH,
                        Variant.NORMAL,
                        Condition.NEAR_MINT,
                        3,
                        10000L,
                        18000L
                );

        importation.addItem(firstItem);
        importation.addItem(secondItem);

        ImportCostSummary result = calculator.calculateBaseCosts(importation);

        assertEquals(5L, result.totalCardQuantity());

        assertEquals(90000L, result.merchandiseCostClp());

        assertEquals(140000L, result.totalSharedCostClp());

        assertEquals(28000L, result.sharedCostPerCardClp());

        assertEquals(230000L, result.baseCostClp());
    }

    @Test
    void shouldDivideSharedCostsByTotalQuantityNotItemCount() {

        Importation importation =
                new Importation(
                        ImportOrigin.JAPAN,
                        20000L,
                        40000L,
                        0L,
                        0L,
                        new BigDecimal("200.00")
                );

        Card firstCard = new Card();
        firstCard.setName("Carta A");

        Card secondCard = new Card();
        secondCard.setName("Carta B");

        importation.addItem(
                new ImportItem(
                        firstCard,
                        Language.JAPANESE,
                        Variant.NORMAL,
                        Condition.NEAR_MINT,
                        4,
                        5000L,
                        10000L
                )
        );

        importation.addItem(
                new ImportItem(
                        secondCard,
                        Language.JAPANESE,
                        Variant.NORMAL,
                        Condition.NEAR_MINT,
                        2,
                        10000L,
                        18000L
                )
        );

        ImportCostSummary result =
                calculator.calculateBaseCosts(importation);

        assertEquals(
                6L,
                result.totalCardQuantity()
        );

        assertEquals(
                60000L,
                result.totalSharedCostClp()
        );

        assertEquals(
                10000L,
                result.sharedCostPerCardClp()
        );
    }

    @Test
    void shouldRejectImportationWithoutItems() {

        Importation importation =
                new Importation(
                        ImportOrigin.USA,
                        10000L,
                        20000L,
                        0L,
                        0L,
                        new BigDecimal("100.00")
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> calculator
                                .calculateBaseCosts(importation)
                );

        assertTrue(
                exception.getMessage()
                        .contains("al menos una carta")
        );
    }
}