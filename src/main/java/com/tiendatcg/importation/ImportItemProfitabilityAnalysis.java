package com.tiendatcg.importation;

public record ImportItemProfitabilityAnalysis(
        ImportItemCostAnalysis cost,
        SaleScenarioAnalysis quick,
        SaleScenarioAnalysis normal,
        SaleScenarioAnalysis slow,
        ImportViability viability
) {
}