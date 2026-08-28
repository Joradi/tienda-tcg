package com.tiendatcg.importation;

public record ImportItemCostAnalysis(
        Long importItemId,
        Long cardId,
        String cardName,
        int quantity,
        long purchaseUnitPriceClp,
        long purchaseTotalClp,
        long sharedCostAllocatedClp,
        long sharedCostPerUnitClp,
        long allocatedTaxClp,
        long taxPerUnitClp,
        long landedCostTotalClp,
        long landedCostUnitClp,
        long localReferencePriceClp
) {
}