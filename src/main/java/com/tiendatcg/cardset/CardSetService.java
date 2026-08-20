package com.tiendatcg.cardset;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CardSetService{

    private final CardSetRepository cardSetRepository;

    public CardSetService(CardSetRepository cardSetRepository) {
        this.cardSetRepository = cardSetRepository;
    }

    public CardSet saveCardSet(CardSet cardSet)
    {
        return cardSetRepository.save(cardSet);
    }

    public Optional<CardSet> findByExternalId(String externalId)
    {
        return cardSetRepository.findByExternalId(externalId);
    }

    @Transactional
    public void syncCardSets(CardSetResponse response)
    {
        for(CardSetDto dto : response.getData())
        {
            Optional<CardSet> existingCardSet =
                    cardSetRepository.findByExternalId(dto.getId());

            if(existingCardSet.isPresent())
            {
                CardSet cardSet = existingCardSet.get();
                cardSet.setName(dto.getName());
                cardSet.setPrintedTotal(dto.getPrintedTotal());

                cardSetRepository.save(cardSet);
            }
            else{

                CardSet cardSet = new CardSet(
                        dto.getId(),
                        dto.getName(),
                        dto.getPrintedTotal()
                );

                cardSetRepository.save(cardSet);
            }
        }
    }
}
