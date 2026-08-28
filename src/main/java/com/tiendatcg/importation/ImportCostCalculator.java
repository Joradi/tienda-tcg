package com.tiendatcg.importation;

import org.springframework.stereotype.Component;

@Component
public class ImportCostCalculator {

    public ImportCostSummary calculateBaseCosts(Importation importation) {
        long totalCardQuantity = importation.getTotalCardQuantity();

        if (totalCardQuantity <= 0)
        {
            throw new IllegalArgumentException("La importación debe contener al menos una carta");
        }

        long merchandiseCostClp = calculateMerchandiseCost(importation);

        long totalSharedCostClp = Math.addExact(Math.addExact(importation.getProxyCostClp(),importation.getFreightCostClp()),
                        Math.addExact(importation.getInsuranceCostClp(), importation.getOtherSharedCostClp()));

        long sharedCostPerCardClp = totalSharedCostClp / totalCardQuantity;
        long baseCostClp = Math.addExact(merchandiseCostClp, totalSharedCostClp);

        return new ImportCostSummary(
                totalCardQuantity,
                merchandiseCostClp,
                importation.getProxyCostClp(),
                importation.getFreightCostClp(),
                importation.getInsuranceCostClp(),
                importation.getOtherSharedCostClp(),
                totalSharedCostClp,
                sharedCostPerCardClp,
                baseCostClp
        );
    }

    private long calculateMerchandiseCost(Importation importation)
    {
        long total = 0L;
        for (ImportItem item : importation.getItems()) {
            long itemCost = Math.multiplyExact(item.getPurchaseUnitPriceClp(),item.getQuantity());
            total = Math.addExact(total, itemCost);
        }
        return total;
    }
}