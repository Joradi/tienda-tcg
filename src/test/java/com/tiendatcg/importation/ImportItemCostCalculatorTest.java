package com.tiendatcg.importation;

import com.tiendatcg.card.Card;
import com.tiendatcg.product.Condition;
import com.tiendatcg.product.Language;
import com.tiendatcg.product.Variant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ImportItemCostCalculatorTest {

    private ImportItemCostCalculator calculator;
    private ImportTaxCalculator taxCalculator;

    @BeforeEach
    void setUp()
    {
        ImportCostCalculator costCalculator = new ImportCostCalculator();

        taxCalculator = new ImportTaxCalculator(costCalculator, new BigDecimal("500"), new BigDecimal("0.06"), new BigDecimal("0.19"));

        calculator = new ImportItemCostCalculator(costCalculator, taxCalculator);
    }

    @Test
    void shouldSplitLogisticsEquallyButTaxesByCustomsValue()
    {
        Importation importation = new Importation(
                        ImportOrigin.USA,
                        10000L,
                        20000L,
                        0L,
                        0L,
                        new BigDecimal("501.00")
                );

        Card cheapCard = new Card();
        cheapCard.setName("Cheap Card");

        Card expensiveCard = new Card();
        expensiveCard.setName("Expensive Card");

        importation.addItem(new ImportItem(
                        cheapCard,
                        Language.ENGLISH,
                        Variant.NORMAL,
                        Condition.NEAR_MINT,
                        1,
                        10000L,
                        30000L
                )
        );

        importation.addItem(new ImportItem(
                        expensiveCard,
                        Language.ENGLISH,
                        Variant.NORMAL,
                        Condition.NEAR_MINT,
                        1,
                        70000L,
                        120000L
                )
        );

        List<ImportItemCostAnalysis> result = calculator.calculateItemCosts(importation);

        ImportItemCostAnalysis cheap = result.get(0);

        ImportItemCostAnalysis expensive = result.get(1);

        assertEquals(15000L, cheap.sharedCostAllocatedClp());

        assertEquals(15000L, expensive.sharedCostAllocatedClp());

        assertEquals(5228L, cheap.allocatedTaxClp());

        assertEquals(20912L, expensive.allocatedTaxClp());

        assertEquals(30228L, cheap.landedCostUnitClp());

        assertEquals(105912L, expensive.landedCostUnitClp());
    }

    @Test
    void shouldAllocateSharedCostsAccordingToTotalCardQuantity()
    {
        Importation importation = new Importation(
                        ImportOrigin.JAPAN,
                        10000L,
                        50000L,
                        0L,
                        0L,
                        new BigDecimal("200.00")
                );

        Card firstCard = new Card();
        firstCard.setName("Card A");

        Card secondCard = new Card();
        secondCard.setName("Card B");

        importation.addItem(new ImportItem(
                        firstCard,
                        Language.JAPANESE,
                        Variant.NORMAL,
                        Condition.NEAR_MINT,
                        4,
                        10000L,
                        20000L
                )
        );

        importation.addItem(new ImportItem(
                        secondCard,
                        Language.JAPANESE,
                        Variant.NORMAL,
                        Condition.NEAR_MINT,
                        2,
                        20000L,
                        35000L
                )
        );

        List<ImportItemCostAnalysis> result = calculator.calculateItemCosts(importation);

        assertEquals(40000L, result.get(0).sharedCostAllocatedClp());

        assertEquals(20000L, result.get(1).sharedCostAllocatedClp());

        assertEquals(10000L, result.get(0).sharedCostPerUnitClp());

        assertEquals(10000L, result.get(1).sharedCostPerUnitClp());
    }

    @Test
    void shouldPreserveExactTotalAfterAllocations()
     {
        Importation importation =
                new Importation(
                        ImportOrigin.USA,
                        10000L,
                        20000L,
                        0L,
                        0L,
                        new BigDecimal("501.00")
                );

        Card firstCard = new Card();
        firstCard.setName("Card A");

        Card secondCard = new Card();
        secondCard.setName("Card B");

        importation.addItem(new ImportItem(
                        firstCard,
                        Language.ENGLISH,
                        Variant.NORMAL,
                        Condition.NEAR_MINT,
                        1,
                        10000L,
                        30000L
                )
        );

        importation.addItem(new ImportItem(
                        secondCard,
                        Language.ENGLISH,
                        Variant.NORMAL,
                        Condition.NEAR_MINT,
                        1,
                        70000L,
                        120000L
                )
        );

        List<ImportItemCostAnalysis> result = calculator.calculateItemCosts(importation);

        long allocatedTaxTotal = result.stream().mapToLong(ImportItemCostAnalysis::allocatedTaxClp).sum();

        long allocatedSharedTotal = result.stream().mapToLong(ImportItemCostAnalysis::sharedCostAllocatedClp).sum();

        long landedTotal = result.stream()
                        .mapToLong(ImportItemCostAnalysis::landedCostTotalClp)
                        .sum();

        ImportTaxSummary taxSummary = taxCalculator.calculateTaxes(importation);

        assertEquals(taxSummary.totalTaxClp(), allocatedTaxTotal);

        assertEquals(30000L, allocatedSharedTotal);

        assertEquals(taxSummary.landedCostTotalClp(), landedTotal);
    }

    @Test
    void shouldPreserveSharedCostRemainderWithoutLosingPesos()
    {
        Importation importation = new Importation(
                        ImportOrigin.USA,
                        100L,
                        0L,
                        0L,
                        0L,
                        new BigDecimal("0.00")
                );

        Card firstCard = new Card();
        firstCard.setName("Card A");

        Card secondCard = new Card();
        secondCard.setName("Card B");

        importation.addItem(new ImportItem(
                        firstCard,
                        Language.ENGLISH,
                        Variant.NORMAL,
                        Condition.NEAR_MINT,
                        2,
                        0L,
                        1000L
                )
        );

        importation.addItem(new ImportItem(
                        secondCard,
                        Language.ENGLISH,
                        Variant.NORMAL,
                        Condition.NEAR_MINT,
                        1,
                        0L,
                        1000L
                )
        );

        List<ImportItemCostAnalysis> result = calculator.calculateItemCosts(importation);

        long sharedTotal = result.stream()
                        .mapToLong(ImportItemCostAnalysis::sharedCostAllocatedClp)
                        .sum();

        assertEquals(100L, sharedTotal);
    }
}