package com.tiendatcg.importation;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ImportationMapper {

    public ImportationResponseDto toResponseDto(Importation importation)
    {
        List<ImportItemResponseDto> items = importation.getItems()
                .stream()
                .map(this::toItemResponseDto)
                .toList();

        return new ImportationResponseDto(
                importation.getId(),
                importation.getOrigin().name(),
                importation.getProxyCostClp(),
                importation.getFreightCostClp(),
                importation.getInsuranceCostClp(),
                importation.getOtherSharedCostClp(),
                importation.getCustomsValueUsd(),
                importation.getTotalCardQuantity(),
                importation.getCreatedAt(),
                items
        );
    }

    private ImportItemResponseDto toItemResponseDto(ImportItem item)
    {
        return new ImportItemResponseDto(
                item.getId(),
                item.getCard().getId(),
                item.getCard().getName(),
                item.getLanguage().name(),
                item.getVariant().name(),
                item.getCondition().name(),
                item.getQuantity(),
                item.getPurchaseUnitPriceClp(),
                item.getLocalReferencePriceClp()
        );
    }
}