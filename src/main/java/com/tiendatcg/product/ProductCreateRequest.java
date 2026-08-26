package com.tiendatcg.product;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public class ProductCreateRequest {
    @NotNull
    private Long cardId;
    @NotNull
    private Language language;
    @NotNull
    private Variant variant;
    @NotNull
    private Condition condition;
    @PositiveOrZero
    private int stock;
    @Positive
    private long price;

    public ProductCreateRequest() {
    }

    public Long getCardId() {
        return cardId;
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
}
