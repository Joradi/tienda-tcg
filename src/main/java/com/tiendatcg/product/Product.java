package com.tiendatcg.product;

import com.tiendatcg.card.Card;
import jakarta.persistence.*;


import java.time.LocalDate;

@Entity
@Table(name = "product",
uniqueConstraints = @UniqueConstraint(
        name = "uk_product_card_language_variant_condition",
        columnNames = {"card_id", "language", "variant", "condition"}
))
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Language language;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Variant variant;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Condition condition;
    private int stock;
    private long price;
    private LocalDate lastPriceReview;

    public Product() {
    }

    public Product(Card card, Language language, Variant variant, Condition condition, int stock, long price, LocalDate lastPriceReview) {
        this.card = card;
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

    public Card getCard() {
        return card;
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

    public void setCard(Card card) {
        this.card = card;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public void setVariant(Variant variant) {
        this.variant = variant;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public void setLastPriceReview(LocalDate lastPriceReview) {
        this.lastPriceReview = lastPriceReview;
    }
}
