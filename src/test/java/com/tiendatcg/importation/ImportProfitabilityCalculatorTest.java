package com.tiendatcg.importation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ImportProfitabilityCalculatorTest {

    private ImportItemCostCalculator itemCostCalculator;
    private ImportProfitabilityCalculator calculator;
    private Importation importation;

    @BeforeEach
    void setUp() {

        itemCostCalculator = mock(ImportItemCostCalculator.class);

        importation = mock(Importation.class);

        calculator = new ImportProfitabilityCalculator(itemCostCalculator,
                        new BigDecimal("0.90"),
                        new BigDecimal("1.00"),
                        new BigDecimal("1.10"),
                        new BigDecimal("0.25"),
                        new BigDecimal("0.15")
                );
    }

    @Test
    void shouldClassifyHighViability()
    {
        ImportItemCostAnalysis cost =
                createCostAnalysis(60000L, 100000L);

        when(itemCostCalculator.calculateItemCosts(importation))
                .thenReturn(List.of(cost));

        ImportItemProfitabilityAnalysis result =
                calculator.analyze(importation)
                        .getFirst();

        assertEquals(ImportViability.HIGH, result.viability());

        assertEquals(90000L, result.quick().salePriceClp());

        assertEquals(30000L, result.quick().profitPerUnitClp());

        assertEquals(new BigDecimal("0.5000"), result.quick().markup());

        assertEquals(new BigDecimal("0.3333"), result.quick().margin());

        assertEquals(100000L, result.normal().salePriceClp());

        assertEquals(40000L, result.normal().profitPerUnitClp());

        assertEquals(new BigDecimal("0.6667"), result.normal().markup());

        assertEquals(new BigDecimal("0.4000"), result.normal().margin());

        assertEquals(110000L, result.slow().salePriceClp());

        assertEquals(50000L, result.slow().profitPerUnitClp());
    }

    @Test
    void shouldClassifyMediumViability()
    {
        ImportItemCostAnalysis cost = createCostAnalysis(80000L, 100000L);

        when(itemCostCalculator.calculateItemCosts(importation))
                .thenReturn(List.of(cost));

        ImportItemProfitabilityAnalysis result = calculator.analyze(importation).getFirst();

        assertEquals(ImportViability.MEDIUM, result.viability());

        assertEquals(new BigDecimal("0.2000"), result.normal().margin());

        assertEquals(10000L, result.quick().profitPerUnitClp());
    }

    @Test
    void shouldClassifyLowViability()
    {
        ImportItemCostAnalysis cost = createCostAnalysis(88000L, 100000L);

        when(itemCostCalculator.calculateItemCosts(importation))
                .thenReturn(List.of(cost));

        ImportItemProfitabilityAnalysis result =
                calculator.analyze(importation)
                        .getFirst();

        assertEquals(ImportViability.LOW, result.viability());

        assertEquals(new BigDecimal("0.1200"), result.normal().margin());

        assertTrue(result.normal().profitPerUnitClp() > 0);
    }

    @Test
    void shouldClassifyNotViableWhenNormalSaleDoesNotGenerateProfit()
    {
        ImportItemCostAnalysis cost = createCostAnalysis(100000L, 100000L);

        when(itemCostCalculator.calculateItemCosts(importation))
                .thenReturn(List.of(cost));

        ImportItemProfitabilityAnalysis result = calculator.analyze(importation).getFirst();

        assertEquals(ImportViability.NOT_VIABLE, result.viability());

        assertEquals(0L, result.normal().profitPerUnitClp());

        assertEquals(new BigDecimal("0.0000"), result.normal().margin());
    }

    @Test
    void shouldRejectAnalysisWithoutLocalReferencePrice()
    {
        ImportItemCostAnalysis cost = createCostAnalysis(50000L, 0L);

        when(itemCostCalculator.calculateItemCosts(importation))
                .thenReturn(List.of(cost));

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class,
                        () -> calculator.analyze(importation));

        assertTrue(exception.getMessage().contains("referencia local"));
    }

    private ImportItemCostAnalysis createCostAnalysis(long landedCostUnitClp, long localReferencePriceClp)
    {
        return new ImportItemCostAnalysis(
                null,
                null,
                "Test Card",
                1,
                0L,
                0L,
                0L,
                0L,
                0L,
                0L,
                landedCostUnitClp,
                landedCostUnitClp,
                localReferencePriceClp
        );
    }
}