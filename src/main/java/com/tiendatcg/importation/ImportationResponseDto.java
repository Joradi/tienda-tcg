package com.tiendatcg.importation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ImportationResponseDto(
        Long id,
        String origin,
        long proxyCostClp,
        long freightCostClp,
        long insuranceCostClp,
        long otherSharedCostClp,
        BigDecimal customsValueUsd,
        long totalCardQuantity,
        LocalDateTime createdAt,
        List<ImportItemResponseDto> items
) {
}