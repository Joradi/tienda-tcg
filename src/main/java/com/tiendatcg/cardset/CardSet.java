package com.tiendatcg.cardset;

import jakarta.persistence.*;

@Entity
@Table(name = "cards_set")
public class CardSet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String externalId;
    private String name;
    private int printedTotal;

    public CardSet() {
    }

    public CardSet(String externalId, String name, int printedTotal) {
        this.externalId = externalId;
        this.name = name;
        this.printedTotal = printedTotal;
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

    public int getPrintedTotal() {
        return printedTotal;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrintedTotal(int printedTotal) {
        this.printedTotal = printedTotal;
    }
}
