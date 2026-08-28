package com.tiendatcg.importation;

public record ImportScenarioComparison(
        ImportScenarioSummary firstScenario,
        ImportScenarioSummary secondScenario,
        long landedCostDifferenceClp,
        long quickProfitDifferenceClp,
        long normalProfitDifferenceClp,
        long slowProfitDifferenceClp
) {
}