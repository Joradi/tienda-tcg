package com.tiendatcg.importation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ImportationAnalysisCalculatorTest {

    private ImportCostCalculator costCalculator;
    private ImportTaxCalculator taxCalculator;
    private ImportProfitabilityCalculator profitabilityCalculator;

    private ImportationAnalysisCalculator calculator;

    private Importation importation;

    @BeforeEach
    void setUp()
    {

        costCalculator = mock(ImportCostCalculator.class);
        taxCalculator = mock(ImportTaxCalculator.class);

        profitabilityCalculator = mock(ImportProfitabilityCalculator.class);

        importation = mock(Importation.class);

        calculator = new ImportationAnalysisCalculator(costCalculator, taxCalculator, profitabilityCalculator);
    }

    @Test
    void shouldCalculateGlobalImportationAnalysis()
    {
        ImportCostSummary costSummary = new ImportCostSummary(
                        3L,
                        2000L,
                        100L,
                        200L,
                        100L,
                        100L,
                        500L,
                        166L,
                        2500L
                );

        ImportTaxSummary taxSummary = new ImportTaxSummary(
                        2300L,
                        false,
                        0L,
                        2300L,
                        437L,
                        437L,
                        145L,
                        2937L,
                        979L
                );

        ImportItemProfitabilityAnalysis first = createItemAnalysis(
                        2,
                        900L,
                        1000L,
                        1100L,
                        ImportViability.HIGH
                );

        ImportItemProfitabilityAnalysis second = createItemAnalysis(
                        1,
                        1800L,
                        2000L,
                        2200L,
                        ImportViability.LOW
                );

        when(costCalculator.calculateBaseCosts(importation)).thenReturn(costSummary);

        when(taxCalculator.calculateTaxes(importation)).thenReturn(taxSummary);

        when(profitabilityCalculator.analyze(importation)).thenReturn(List.of(first, second));

        ImportationAnalysis result = calculator.analyze(importation);

        assertEquals(2, result.totalItemCount());

        assertEquals(3L, result.totalCardQuantity());

        assertEquals(2000L, result.merchandiseCostClp());

        assertEquals(500L, result.totalSharedCostClp());

        assertEquals(437L, result.totalTaxClp());

        assertEquals(2937L, result.landedCostTotalClp());

        assertEquals(3600L, result.quick().potentialRevenueClp());

        assertEquals(663L, result.quick().potentialProfitClp());

        assertEquals(new BigDecimal("0.1842"), result.quick().margin());

        assertEquals(4000L, result.normal().potentialRevenueClp());

        assertEquals(1063L, result.normal().potentialProfitClp());

        assertEquals(new BigDecimal("0.2658"), result.normal().margin());

        assertEquals(4400L, result.slow().potentialRevenueClp());

        assertEquals(1463L, result.slow().potentialProfitClp());

        assertEquals(new BigDecimal("0.3325"), result.slow().margin());

        assertEquals(2L, result.highCardQuantity());

        assertEquals(0L, result.mediumCardQuantity());

        assertEquals(1L, result.lowCardQuantity());

        assertEquals(0L, result.notViableCardQuantity());
    }

    @Test
    void shouldUseExactGlobalLandedCostInsteadOfRoundedUnitCosts()
    {
        ImportCostSummary costSummary = new ImportCostSummary(
                        2L,
                        1000L,
                        0L,
                        0L,
                        0L,
                        0L,
                        0L,
                        0L,
                        1000L
                );

        ImportTaxSummary taxSummary = new ImportTaxSummary(
                        1000L,
                        false,
                        0L,
                        1000L,
                        1L,
                        1L,
                        0L,
                        1001L,
                        501L
                );

        ImportItemProfitabilityAnalysis item = createItemAnalysis(
                        2,
                        900L,
                        1000L,
                        1100L,
                        ImportViability.HIGH
                );

        when(costCalculator.calculateBaseCosts(importation)).thenReturn(costSummary);

        when(taxCalculator.calculateTaxes(importation)).thenReturn(taxSummary);

        when(profitabilityCalculator.analyze(importation)).thenReturn(List.of(item));

        ImportationAnalysis result = calculator.analyze(importation);

        assertEquals(2000L, result.normal().potentialRevenueClp());

        assertEquals(999L, result.normal().potentialProfitClp());
    }

    @Test
    void shouldCountCardQuantitiesForEveryViabilityLevel()
    {

        ImportCostSummary costSummary =
                new ImportCostSummary(
                        10L,
                        0L,
                        0L,
                        0L,
                        0L,
                        0L,
                        0L,
                        0L,
                        0L
                );

        ImportTaxSummary taxSummary =
                new ImportTaxSummary(
                        0L,
                        false,
                        0L,
                        0L,
                        0L,
                        0L,
                        0L,
                        0L,
                        0L
                );

        when(costCalculator.calculateBaseCosts(importation)).thenReturn(costSummary);

        when(taxCalculator.calculateTaxes(importation)).thenReturn(taxSummary);

        when(profitabilityCalculator.analyze(importation)).thenReturn(
                List.of(
                        createItemAnalysis(
                                4,
                                100L,
                                100L,
                                100L,
                                ImportViability.HIGH
                        ),
                        createItemAnalysis(
                                3,
                                100L,
                                100L,
                                100L,
                                ImportViability.MEDIUM
                        ),
                        createItemAnalysis(
                                2,
                                100L,
                                100L,
                                100L,
                                ImportViability.LOW
                        ),
                        createItemAnalysis(
                                1,
                                100L,
                                100L,
                                100L,
                                ImportViability.NOT_VIABLE
                        )
                )
        );

        ImportationAnalysis result =calculator.analyze(importation);

        assertEquals(4L, result.highCardQuantity());

        assertEquals(3L, result.mediumCardQuantity());

        assertEquals(2L, result.lowCardQuantity());

        assertEquals(1L, result.notViableCardQuantity());
    }

    private ImportItemProfitabilityAnalysis createItemAnalysis(int quantity, long quickPrice, long normalPrice, long slowPrice, ImportViability viability)
    {
        ImportItemCostAnalysis cost = new ImportItemCostAnalysis(
                        null,
                        null,
                        "Test Card",
                        quantity,
                        0L,
                        0L,
                        0L,
                        0L,
                        0L,
                        0L,
                        0L,
                        0L,
                        normalPrice
                );

        SaleScenarioAnalysis quick = new SaleScenarioAnalysis(
                        SaleStrategy.QUICK,
                        quickPrice,
                        0L,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                );

        SaleScenarioAnalysis normal = new SaleScenarioAnalysis(
                        SaleStrategy.NORMAL,
                        normalPrice,
                        0L,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                );

        SaleScenarioAnalysis slow = new SaleScenarioAnalysis(
                        SaleStrategy.SLOW,
                        slowPrice,
                        0L,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                );

        return new ImportItemProfitabilityAnalysis(cost, quick, normal, slow, viability);
    }
}