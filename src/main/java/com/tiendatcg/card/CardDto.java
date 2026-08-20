package com.tiendatcg.card;

import com.tiendatcg.cardset.CardSetDto;

import java.util.List;

public class CardDto {
    private String id;
    private String name;
    private String supertype;
    private List<String> subtypes;
    private String artist;
    private String number;
    private String rarity;
    private CardImagesDto images;
    private CardSetDto set;

    public CardDto() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSupertype() {
        return supertype;
    }

    public void setSupertype(String supertype) {
        this.supertype = supertype;
    }

    public List<String> getSubtypes() {
        return subtypes;
    }

    public void setSubtypes(List<String> subtypes) {
        this.subtypes = subtypes;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getRarity() {
        return rarity;
    }

    public void setRarity(String rarity) {
        this.rarity = rarity;
    }

    public CardImagesDto getImages() {
        return images;
    }

    public void setImages(CardImagesDto images) {
        this.images = images;
    }

    public CardSetDto getSet() {
        return set;
    }

    public void setSet(CardSetDto set) {
        this.set = set;
    }
}
