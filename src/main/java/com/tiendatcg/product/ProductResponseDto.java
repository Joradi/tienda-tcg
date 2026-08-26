package com.tiendatcg.product;

import java.time.LocalDate;

public class ProductResponseDto {
    private Long id;
    private Long cardId;
    private String cardName;
    private String imageUrl;
    private Language language;
    private Variant variant;
    private Condition condition;
    private int stock;
    private long price;
    private LocalDate lastPriceReview;

    public ProductResponseDto(Long id, Long cardId, String cardName, String imageUrl, Language language, Variant variant, Condition condition, int stock, long price, LocalDate lastPriceReview) {
        this.id = id;
        this.cardId = cardId;
        this.cardName = cardName;
        this.imageUrl = imageUrl;
        this.language = language;
        this.variant = variant;
        this.condition = condition;
        this.stock = stock;
        this.price = price;
        this.lastPriceReview = lastPriceReview;
    }

    public Long getId() {
        return id;
    }

    public Long getCardId() {
        return cardId;
    }

    public String getCardName() {
        return cardName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Language getLanguage() {
        return language;
    }

    public Variant getVariant() {
        return variant;
    }

    public Condition getCondition() {
        return condition;
    }

    public int getStock() {
        return stock;
    }

    public long getPrice() {
        return price;
    }

    public LocalDate getLastPriceReview() {
        return lastPriceReview;
    }
}
