package com.tiendatcg.importation;

public record ImportScenarioSummary(
        int importationCount,
        long totalCardQuantity,
        long merchandiseCostClp,
        long totalSharedCostClp,
        long totalTaxClp,
        long landedCostTotalClp,
        ImportationStrategySummary quick,
        ImportationStrategySummary normal,
        ImportationStrategySummary slow,
        long highCardQuantity,
        long mediumCardQuantity,
        long lowCardQuantity,
        long notViableCardQuantity
) {
}