package com.tiendatcg.importation;

public record ImportItemResponseDto(
        Long id,
        Long cardId,
        String cardName,
        String language,
        String variant,
        String condition,
        int quantity,
        long purchaseUnitPriceClp,
        long localReferencePriceClp
) {
}