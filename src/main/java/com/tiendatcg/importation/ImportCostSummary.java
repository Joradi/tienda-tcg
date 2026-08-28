package com.tiendatcg.importation;

public record ImportCostSummary(
        long totalCardQuantity,
        long merchandiseCostClp,
        long proxyCostClp,
        long freightCostClp,
        long insuranceCostClp,
        long otherSharedCostClp,
        long totalSharedCostClp,
        long sharedCostPerCardClp,
        long baseCostClp
) {
}