package com.tiendatcg.importation;

public record ImportTaxSummary(
        long customsBaseClp,
        boolean adValoremApplied,
        long adValoremClp,
        long vatBaseClp,
        long importVatClp,
        long totalTaxClp,
        long taxPerCardClp,
        long landedCostTotalClp,
        long averageLandedCostPerCardClp
) {
}