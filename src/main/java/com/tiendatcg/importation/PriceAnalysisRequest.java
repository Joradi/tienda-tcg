package com.tiendatcg.importation;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record PriceAnalysisRequest(
        @PositiveOrZero long landedCostUnitClp,
        @Positive long localReferencePriceClp
) {
}