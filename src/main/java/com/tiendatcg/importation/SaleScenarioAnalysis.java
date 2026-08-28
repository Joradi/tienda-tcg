package com.tiendatcg.importation;

import java.math.BigDecimal;

public record SaleScenarioAnalysis(
        SaleStrategy strategy,
        long salePriceClp,
        long profitPerUnitClp,
        BigDecimal markup,
        BigDecimal margin
) {
}