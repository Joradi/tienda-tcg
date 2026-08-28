package com.tiendatcg.importation;

import java.math.BigDecimal;

public record ImportationStrategySummary(
        SaleStrategy strategy,
        long potentialRevenueClp,
        long potentialProfitClp,
        BigDecimal margin
) {
}