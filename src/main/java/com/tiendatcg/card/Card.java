package com.tiendatcg.card;

import com.tiendatcg.cardset.CardSet;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "cards")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String externalId;
    private String name;
    private String superType;
    @ElementCollection
    private List<String> subTypes;
    private String illustrator;
    private String number;
    private String rarity;
    private String imageUrl;
    @ManyToOne
    @JoinColumn(name = "card_set_id")
    private CardSet cardSet;

    public Card(String externalId, String name, String superType, List<String> subTypes, String illustrator, String number, String rarity, String imageUrl, CardSet cardSet) {
        this.externalId = externalId;
        this.name = name;
        this.superType = superType;
        this.subTypes = subTypes;
        this.illustrator = illustrator;
        this.number = number;
        this.rarity = rarity;
        this.imageUrl = imageUrl;
        this.cardSet = cardSet;
    }

    public Card() {
    }

    public Long getId() {
        return id;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getName() {
        return name;
    }

    public String getSuperType() {
        return superType;
    }

    public List<String> getSubTypes() {
        return subTypes;
    }

    public String getIllustrator() {
        return illustrator;
    }

    public String getNumber() {
        return number;
    }

    public String getRarity() {
        return rarity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public CardSet getCardSet() {
        return cardSet;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSuperType(String superType) {
        this.superType = superType;
    }

    public void setSubTypes(List<String> subTypes) {
        this.subTypes = subTypes;
    }

    public void setIllustrator(String illustrator) {
        this.illustrator = illustrator;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setRarity(String rarity) {
        this.rarity = rarity;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setCardSet(CardSet cardSet) {
        this.cardSet = cardSet;
    }
}
