package com.tiendatcg.importation;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record ImportScenarioComparisonRequest(
        @NotEmpty
        List<@NotNull @Positive Long> firstScenarioImportationIds,
        @NotEmpty
        List<@NotNull @Positive Long> secondScenarioImportationIds
) {
}