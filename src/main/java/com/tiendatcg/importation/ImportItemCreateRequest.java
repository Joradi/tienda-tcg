package com.tiendatcg.importation;

import com.tiendatcg.product.Condition;
import com.tiendatcg.product.Language;
import com.tiendatcg.product.Variant;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record ImportItemCreateRequest(
        @NotNull
        @Positive
        Long cardId,
        @NotNull
        Language language,
        @NotNull
        Variant variant,
        @NotNull
        Condition condition,
        @Positive
        int quantity,
        @PositiveOrZero
        long purchaseUnitPriceClp,
        @PositiveOrZero
        long localReferencePriceClp
) {
}