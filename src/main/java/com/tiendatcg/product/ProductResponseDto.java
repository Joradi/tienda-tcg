package com.tiendatcg.product;

import java.time.LocalDate;
import java.util.List;

public class ProductResponseDto {

    private Long id;
    private Long cardId;
    private String cardName;
    private String imageUrl;

    private String cardNumber;
    private String setName;
    private int setPrintedTotal;
    private String illustrator;
    private String rarity;
    private String superType;
    private List<String> subTypes;

    private Language language;
    private Variant variant;
    private Condition condition;
    private int stock;
    private long price;
    private LocalDate lastPriceReview;

    public ProductResponseDto(
            Long id,
            Long cardId,
            String cardName,
            String imageUrl,
            String cardNumber,
            String setName,
            int setPrintedTotal,
            String illustrator,
            String rarity,
            String superType,
            List<String> subTypes,
            Language language,
            Variant variant,
            Condition condition,
            int stock,
            long price,
            LocalDate lastPriceReview)
    {
        this.id = id;
        this.cardId = cardId;
        this.cardName = cardName;
        this.imageUrl = imageUrl;
        this.cardNumber = cardNumber;
        this.setName = setName;
        this.setPrintedTotal = setPrintedTotal;
        this.illustrator = illustrator;
        this.rarity = rarity;
        this.superType = superType;
        this.subTypes = subTypes;
        this.language = language;
        this.variant = variant;
        this.condition = condition;
        this.stock = stock;
        this.price = price;
        this.lastPriceReview = lastPriceReview;
    }

    public Long getId()
    {
        return id;
    }

    public Long getCardId()
    {
        return cardId;
    }

    public String getCardName()
    {
        return cardName;
    }

    public String getImageUrl()
    {
        return imageUrl;
    }

    public String getCardNumber()
    {
        return cardNumber;
    }

    public String getSetName()
    {
        return setName;
    }

    public int getSetPrintedTotal()
    {
        return setPrintedTotal;
    }

    public String getIllustrator()
    {
        return illustrator;
    }

    public String getRarity()
    {
        return rarity;
    }

    public String getSuperType()
    {
        return superType;
    }

    public List<String> getSubTypes()
    {
        return subTypes;
    }

    public Language getLanguage()
    {
        return language;
    }

    public Variant getVariant()
    {
        return variant;
    }

    public Condition getCondition()
    {
        return condition;
    }

    public int getStock()
    {
        return stock;
    }

    public long getPrice()
    {
        return price;
    }

    public LocalDate getLastPriceReview()
    {
        return lastPriceReview;
    }
}
