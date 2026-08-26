package com.tiendatcg.card;

import com.tiendatcg.cardset.CardSet;
import com.tiendatcg.cardset.CardSetService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CardService {
    private final CardRepository cardRepository;
    private final CardSetService cardSetService;

    public CardService(CardRepository cardRepository, CardSetService cardSetService) {
        this.cardRepository = cardRepository;
        this.cardSetService = cardSetService;
    }

    public Card saveCard(Card card)
    {
        return cardRepository.save(card);
    }

    public Optional<Card> findByExternalId(String externalId)
    {
        return cardRepository.findByExternalId(externalId);
    }

    @Transactional
    public void syncCards(CardResponse response)
    {
        for (CardDto dto : response.getData())
        {
            CardSet cardSet = cardSetService
                    .findByExternalId(dto.getSet().getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "CardSet no encontrado: " + dto.getSet().getId()
                    ));

            Optional<Card> existingCard =
                    cardRepository.findByExternalId(dto.getId());

            Card card;
            if(existingCard.isPresent())
            {
                card = existingCard.get();

            }
            else{
                card = new Card();
                card.setExternalId(dto.getId());
            }

            card.setName(dto.getName());
            card.setSuperType(dto.getSupertype());
            card.setSubTypes(dto.getSubtypes());
            card.setIllustrator(dto.getArtist());
            card.setNumber(dto.getNumber());
            card.setRarity(dto.getRarity());
            card.setImageUrl(dto.getImages().getLarge());
            card.setCardSet(cardSet);

            cardRepository.save(card);
        }
    }

    public List<Card> searchCards(String query)
    {
        String value = query.trim();

        if(value.matches("\\d+/\\d+"))
        {
            String[] parts = value.split("/");

            int cardNumber = Integer.parseInt(parts[0]);
            int printedTotal = Integer.parseInt(parts[1]);

            List<Card> candidates =
                    cardRepository.findByCardSet_PrintedTotal(printedTotal);

            List<Card> results = new ArrayList<>();

            for(Card card : candidates)
            {
                try {
                        int storedNumber = Integer.parseInt(card.getNumber());

                        if(storedNumber == cardNumber)
                        {
                            results.add(card);
                        }
                }
                catch (NumberFormatException ignored)
                {

                }
            }
            return results;
        }

        return cardRepository.findByNameContainingIgnoreCase(value);
    }

    public Optional<Card> findById(Long id)
    {
        return cardRepository.findById(id);
    }


}
