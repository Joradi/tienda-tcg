package com.tiendatcg.card;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RestController
@RequestMapping("/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping("/search")
    public List<CardSearchDto> searchCards(@RequestParam String query)
    {
        return cardService.searchCards(query)
                .stream()
                .map(card -> new CardSearchDto(
                        card.getId(),
                        card.getName(),
                        card.getNumber(),
                        card.getImageUrl(),
                        card.getCardSet().getName(),
                        card.getCardSet().getPrintedTotal()
                ))
                .toList();
    }
}
