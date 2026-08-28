package com.tiendatcg.importation;

import java.util.List;
import java.util.Objects;

public record ImportationAnalysis(

        int totalItemCount,
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
        long notViableCardQuantity,
        List<ImportItemProfitabilityAnalysis> items)
    {
        public ImportationAnalysis
        {
        Objects.requireNonNull(quick, "El resumen QUICK es obligatorio");
        Objects.requireNonNull(normal, "El resumen NORMAL es obligatorio");
        Objects.requireNonNull(slow, "El resumen SLOW es obligatorio");
        Objects.requireNonNull(items, "Los análisis por item son obligatorios");
        items = List.copyOf(items);
    }
}