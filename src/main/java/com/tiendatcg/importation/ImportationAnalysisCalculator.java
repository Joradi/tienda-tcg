package com.tiendatcg.importation;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

@Component
public class ImportationAnalysisCalculator {

    private static final int PERCENTAGE_SCALE = 4;

    private final ImportCostCalculator costCalculator;
    private final ImportTaxCalculator taxCalculator;
    private final ImportProfitabilityCalculator profitabilityCalculator;

    public ImportationAnalysisCalculator(ImportCostCalculator costCalculator, ImportTaxCalculator taxCalculator, ImportProfitabilityCalculator profitabilityCalculator) {
        this.costCalculator = Objects.requireNonNull(costCalculator, "El calculador de costos es obligatorio");
        this.taxCalculator = Objects.requireNonNull(taxCalculator, "El calculador de impuestos es obligatorio");
        this.profitabilityCalculator = Objects.requireNonNull(profitabilityCalculator, "El calculador de rentabilidad es obligatorio");
    }

    public ImportationAnalysis analyze(Importation importation)
    {
        Objects.requireNonNull(importation, "La importación es obligatoria");
        ImportCostSummary costSummary = costCalculator.calculateBaseCosts(importation);
        ImportTaxSummary taxSummary = taxCalculator.calculateTaxes(importation);
        List<ImportItemProfitabilityAnalysis> itemAnalyses = profitabilityCalculator.analyze(importation);
        ImportationStrategySummary quick = calculateStrategySummary(SaleStrategy.QUICK, itemAnalyses, taxSummary.landedCostTotalClp());
        ImportationStrategySummary normal = calculateStrategySummary(SaleStrategy.NORMAL, itemAnalyses, taxSummary.landedCostTotalClp());
        ImportationStrategySummary slow = calculateStrategySummary(SaleStrategy.SLOW, itemAnalyses, taxSummary.landedCostTotalClp());
        long highCardQuantity = calculateQuantityByViability(itemAnalyses, ImportViability.HIGH);
        long mediumCardQuantity = calculateQuantityByViability(itemAnalyses, ImportViability.MEDIUM);
        long lowCardQuantity = calculateQuantityByViability(itemAnalyses, ImportViability.LOW);
        long notViableCardQuantity = calculateQuantityByViability(itemAnalyses, ImportViability.NOT_VIABLE);

        return new ImportationAnalysis(itemAnalyses.size(),
                costSummary.totalCardQuantity(),
                costSummary.merchandiseCostClp(),
                costSummary.totalSharedCostClp(),
                taxSummary.totalTaxClp(),
                taxSummary.landedCostTotalClp(),
                quick,
                normal,
                slow,
                highCardQuantity,
                mediumCardQuantity,
                lowCardQuantity,
                notViableCardQuantity,
                itemAnalyses
        );
    }

    private ImportationStrategySummary calculateStrategySummary(SaleStrategy strategy, List<ImportItemProfitabilityAnalysis> items,
            long landedCostTotalClp)
    {
        long potentialRevenueClp = 0L;
        for (ImportItemProfitabilityAnalysis item : items)
        {
            SaleScenarioAnalysis scenario = getScenario(item, strategy);
            long itemRevenueClp = Math.multiplyExact(scenario.salePriceClp(), item.cost().quantity());
            potentialRevenueClp = Math.addExact(potentialRevenueClp, itemRevenueClp);
        }

        long potentialProfitClp = Math.subtractExact(potentialRevenueClp, landedCostTotalClp);
        BigDecimal margin = calculateMargin(potentialProfitClp, potentialRevenueClp);

        return new ImportationStrategySummary(strategy, potentialRevenueClp, potentialProfitClp, margin);
    }

    private SaleScenarioAnalysis getScenario(ImportItemProfitabilityAnalysis item, SaleStrategy strategy)
    {
        return switch (strategy)
        {
            case QUICK -> item.quick();
            case NORMAL -> item.normal();
            case SLOW -> item.slow();
        };
    }

    private long calculateQuantityByViability(List<ImportItemProfitabilityAnalysis> items, ImportViability viability)
    {
        long quantity = 0L;
        for (ImportItemProfitabilityAnalysis item : items)
        {
            if (item.viability() == viability)
            {
                quantity = Math.addExact(                         quantity, item.cost().quantity());
            }
        }

        return quantity;
    }

    private BigDecimal calculateMargin(long profitClp, long revenueClp)
    {
        if (revenueClp == 0L)
        {
            return BigDecimal.ZERO.setScale(PERCENTAGE_SCALE);
        }

        return BigDecimal.valueOf(profitClp).divide(
                BigDecimal.valueOf(revenueClp),
                PERCENTAGE_SCALE,
                RoundingMode.HALF_UP
                );
    }
}