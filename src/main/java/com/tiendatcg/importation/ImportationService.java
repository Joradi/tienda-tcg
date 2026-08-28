package com.tiendatcg.importation;

import com.tiendatcg.card.Card;
import com.tiendatcg.card.CardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ImportationService {

    private final ImportationRepository importationRepository;
    private final CardRepository cardRepository;
    private final ImportationMapper importationMapper;

    public ImportationService(ImportationRepository importationRepository, CardRepository cardRepository, ImportationMapper importationMapper) {
        this.importationRepository = importationRepository;
        this.cardRepository = cardRepository;
        this.importationMapper = importationMapper;
    }

    @Transactional
    public ImportationResponseDto createImportation(ImportationCreateRequest request)
    {
        Importation importation = new Importation(
                request.origin(),
                request.proxyCostClp(),
                request.freightCostClp(),
                request.insuranceCostClp(),
                request.otherSharedCostClp(),
                request.customsValueUsd()
        );

        for (ImportItemCreateRequest itemRequest : request.items())
        {
            Card card = cardRepository.findById(itemRequest.cardId())
                    .orElseThrow(() -> new ImportationCardNotFoundException(itemRequest.cardId()));

            ImportItem item = new ImportItem(
                    card,
                    itemRequest.language(),
                    itemRequest.variant(),
                    itemRequest.condition(),
                    itemRequest.quantity(),
                    itemRequest.purchaseUnitPriceClp(),
                    itemRequest.localReferencePriceClp()
            );

            importation.addItem(item);
        }

        Importation savedImportation = importationRepository.save(importation);

        return importationMapper.toResponseDto(savedImportation);
    }

    @Transactional(readOnly = true)
    public ImportationResponseDto getImportation(Long id)
    {
        Importation importation = importationRepository.findWithItemsById(id)
                .orElseThrow(() -> new ImportationNotFoundException(id));

        return importationMapper.toResponseDto(importation);
    }

    @Transactional(readOnly = true)
    public List<ImportationResponseDto> getImportations()
    {
        return importationRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(importationMapper::toResponseDto)
                .toList();
    }
}