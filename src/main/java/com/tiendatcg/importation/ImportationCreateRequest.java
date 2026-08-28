package com.tiendatcg.importation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;

public record ImportationCreateRequest(
        @NotNull
        ImportOrigin origin,
        @PositiveOrZero
        long proxyCostClp,
        @PositiveOrZero
        long freightCostClp,
        @PositiveOrZero
        long insuranceCostClp,
        @PositiveOrZero
        long otherSharedCostClp,
        @NotNull
        @DecimalMin(value = "0.00")
        BigDecimal customsValueUsd,
        @NotEmpty
        List<@Valid ImportItemCreateRequest> items
) {
}