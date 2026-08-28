package com.tiendatcg.importation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ImportScenarioCalculatorTest {

    private ImportationAnalysisCalculator analysisCalculator;
    private ImportScenarioCalculator calculator;

    @BeforeEach
    void setUp()
    {
        analysisCalculator = mock(ImportationAnalysisCalculator.class);

        calculator = new ImportScenarioCalculator(analysisCalculator);
    }

    @Test
    void shouldAggregateMultipleImportationsIntoOneScenario()
    {
        Importation first = mock(Importation.class);

        Importation second = mock(Importation.class);

        when(analysisCalculator.analyze(first)).thenReturn(
                createAnalysis(
                        6L,
                        60000L,
                        18000L,
                        10000L,
                        88000L,
                        95000L,
                        105000L,
                        115000L,
                        3L,
                        2L,
                        1L,
                        0L
                ));

        when(analysisCalculator.analyze(second)).thenReturn(
                createAnalysis(
                        4L,
                        40000L,
                        10000L,
                        8000L,
                        58000L,
                        65000L,
                        75000L,
                        85000L,
                        1L,
                        1L,
                        1L,
                        1L
                )
        );

        ImportScenarioSummary result =
                calculator.analyzeScenario(List.of(first, second));

        assertEquals(2, result.importationCount());

        assertEquals(10L, result.totalCardQuantity());

        assertEquals(100000L, result.merchandiseCostClp());

        assertEquals(28000L, result.totalSharedCostClp());

        assertEquals(18000L, result.totalTaxClp());

        assertEquals(146000L, result.landedCostTotalClp());

        assertEquals(160000L, result.quick().potentialRevenueClp());

        assertEquals(14000L, result.quick().potentialProfitClp());

        assertEquals(180000L, result.normal().potentialRevenueClp());

        assertEquals(34000L, result.normal().potentialProfitClp());

        assertEquals(new BigDecimal("0.1889"), result.normal().margin());

        assertEquals(200000L, result.slow().potentialRevenueClp());

        assertEquals(54000L, result.slow().potentialProfitClp());

        assertEquals(4L, result.highCardQuantity());

        assertEquals(3L, result.mediumCardQuantity());

        assertEquals(2L, result.lowCardQuantity());

        assertEquals(1L, result.notViableCardQuantity());
    }

    @Test
    void shouldCompareOneImportationAgainstTwoImportations()
    {

        Importation oneShipment = mock(Importation.class);

        Importation firstSplit = mock(Importation.class);

        Importation secondSplit = mock(Importation.class);

        when(analysisCalculator.analyze(oneShipment)).thenReturn(
                createAnalysis(
                        10L,
                        100000L,
                        30000L,
                        20000L,
                        150000L,
                        160000L,
                        180000L,
                        200000L,
                        4L,
                        3L,
                        2L,
                        1L
                ));

        when(analysisCalculator.analyze(firstSplit)).thenReturn(
                createAnalysis(
                        6L,
                        60000L,
                        18000L,
                        10000L,
                        88000L,
                        95000L,
                        105000L,
                        115000L,
                        3L,
                        2L,
                        1L,
                        0L
                ));

        when(analysisCalculator.analyze(secondSplit)).thenReturn(
                createAnalysis(
                        4L,
                        40000L,
                        10000L,
                        8000L,
                        58000L,
                        65000L,
                        75000L,
                        85000L,
                        1L,
                        1L,
                        1L,
                        1L
                )
        );

        ImportScenarioComparison result = calculator.compare(List.of(oneShipment),
                        List.of(firstSplit, secondSplit));

        assertEquals(
                1,
                result.firstScenario()
                        .importationCount()
        );

        assertEquals(2, result.secondScenario().importationCount());

        assertEquals(-4000L, result.landedCostDifferenceClp());

        assertEquals(4000L, result.quickProfitDifferenceClp());

        assertEquals(4000L, result.normalProfitDifferenceClp());

        assertEquals(4000L, result.slowProfitDifferenceClp());
    }

    @Test
    void shouldRejectEmptyScenario()
    {
        assertThrows(IllegalArgumentException.class,
                () -> calculator.analyzeScenario(List.of()));
    }

    private ImportationAnalysis createAnalysis(long cardQuantity, long merchandiseCostClp, long sharedCostClp, long taxClp,
            long landedCostClp, long quickRevenueClp, long normalRevenueClp, long slowRevenueClp, long highQuantity, long mediumQuantity,
            long lowQuantity, long notViableQuantity)
    {
        ImportationStrategySummary quick = new ImportationStrategySummary(
                        SaleStrategy.QUICK,
                        quickRevenueClp,
                        quickRevenueClp - landedCostClp,
                        BigDecimal.ZERO
                );

        ImportationStrategySummary normal = new ImportationStrategySummary(
                        SaleStrategy.NORMAL,
                        normalRevenueClp,
                        normalRevenueClp - landedCostClp,
                        BigDecimal.ZERO
                );

        ImportationStrategySummary slow = new ImportationStrategySummary(
                        SaleStrategy.SLOW,
                        slowRevenueClp,
                        slowRevenueClp - landedCostClp,
                        BigDecimal.ZERO
                );

        return new ImportationAnalysis(
                1,
                cardQuantity,
                merchandiseCostClp,
                sharedCostClp,
                taxClp,
                landedCostClp,
                quick,
                normal,
                slow,
                highQuantity,
                mediumQuantity,
                lowQuantity,
                notViableQuantity,
                List.of()
        );
    }
}