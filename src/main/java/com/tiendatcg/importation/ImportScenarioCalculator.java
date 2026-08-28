package com.tiendatcg.importation;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

@Component
public class ImportScenarioCalculator {

    private static final int PERCENTAGE_SCALE = 4;

    private final ImportationAnalysisCalculator analysisCalculator;

    public ImportScenarioCalculator(ImportationAnalysisCalculator analysisCalculator)
    {
        this.analysisCalculator = Objects.requireNonNull(analysisCalculator, "El calculador de análisis es obligatorio");
    }

    public ImportScenarioSummary analyzeScenario(List<Importation> importations)
    {
        Objects.requireNonNull(importations, "Las importaciones son obligatorias");

        if (importations.isEmpty())
        {
            throw new IllegalArgumentException("El escenario debe contener al menos una importación");
        }

        long totalCardQuantity = 0L;
        long merchandiseCostClp = 0L;
        long totalSharedCostClp = 0L;
        long totalTaxClp = 0L;
        long landedCostTotalClp = 0L;
        long highCardQuantity = 0L;
        long mediumCardQuantity = 0L;
        long lowCardQuantity = 0L;
        long notViableCardQuantity = 0L;
        long quickRevenueClp = 0L;
        long normalRevenueClp = 0L;
        long slowRevenueClp = 0L;
        for (Importation importation : importations)
        {
            Objects.requireNonNull(importation, "Una importación del escenario no puede ser null");
            ImportationAnalysis analysis = analysisCalculator.analyze(importation);

            totalCardQuantity = Math.addExact(totalCardQuantity, analysis.totalCardQuantity());

            merchandiseCostClp = Math.addExact(merchandiseCostClp, analysis.merchandiseCostClp());

            totalSharedCostClp = Math.addExact(totalSharedCostClp, analysis.totalSharedCostClp());

            totalTaxClp = Math.addExact(totalTaxClp, analysis.totalTaxClp());

            landedCostTotalClp = Math.addExact(landedCostTotalClp, analysis.landedCostTotalClp());

            highCardQuantity = Math.addExact(highCardQuantity, analysis.highCardQuantity());

            mediumCardQuantity = Math.addExact(mediumCardQuantity, analysis.mediumCardQuantity());

            lowCardQuantity = Math.addExact(lowCardQuantity, analysis.lowCardQuantity());

            notViableCardQuantity = Math.addExact(notViableCardQuantity, analysis.notViableCardQuantity());

            quickRevenueClp = Math.addExact(quickRevenueClp, analysis.quick().potentialRevenueClp());

            normalRevenueClp = Math.addExact(normalRevenueClp, analysis.normal().potentialRevenueClp());

            slowRevenueClp = Math.addExact(slowRevenueClp, analysis.slow().potentialRevenueClp());
        }

        ImportationStrategySummary quick = createStrategySummary(SaleStrategy.QUICK, quickRevenueClp, landedCostTotalClp);

        ImportationStrategySummary normal = createStrategySummary(SaleStrategy.NORMAL, normalRevenueClp, landedCostTotalClp);

        ImportationStrategySummary slow = createStrategySummary(SaleStrategy.SLOW, slowRevenueClp, landedCostTotalClp);

        return new ImportScenarioSummary(
                importations.size(),
                totalCardQuantity,
                merchandiseCostClp,
                totalSharedCostClp,
                totalTaxClp,
                landedCostTotalClp,
                quick,
                normal,
                slow,
                highCardQuantity,
                mediumCardQuantity,
                lowCardQuantity,
                notViableCardQuantity
        );
    }

    public ImportScenarioComparison compare(List<Importation> firstScenario, List<Importation> secondScenario)
    {
        ImportScenarioSummary first = analyzeScenario(firstScenario);

        ImportScenarioSummary second = analyzeScenario(secondScenario);

        return new ImportScenarioComparison(first, second,
                Math.subtractExact(second.landedCostTotalClp(), first.landedCostTotalClp()),
                Math.subtractExact(
                        second.quick().potentialProfitClp(),
                        first.quick().potentialProfitClp()),
                Math.subtractExact(
                        second.normal().potentialProfitClp(),
                        first.normal().potentialProfitClp()),
                Math.subtractExact(
                        second.slow().potentialProfitClp(),
                        first.slow().potentialProfitClp()));
    }

    private ImportationStrategySummary createStrategySummary(SaleStrategy strategy, long revenueClp, long landedCostClp)
    {
        long profitClp = Math.subtractExact(revenueClp, landedCostClp);
        BigDecimal margin = calculateMargin(profitClp, revenueClp);
        return new ImportationStrategySummary(strategy, revenueClp, profitClp, margin);
    }

    private BigDecimal calculateMargin(long profitClp, long revenueClp)
    {
        if (revenueClp == 0L)
        {
            return BigDecimal.ZERO.setScale(PERCENTAGE_SCALE);
        }

        return BigDecimal
                .valueOf(profitClp)
                .divide(
                        BigDecimal.valueOf(revenueClp),
                        PERCENTAGE_SCALE,
                        RoundingMode.HALF_UP
                );
    }
}