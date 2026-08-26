package com.tiendatcg.card;

public class CardSearchDto {

    private Long id;
    private String name;
    private String number;
    private String imageUrl;
    private String setName;
    private int printedTotal;

    public CardSearchDto(Long id, String name, String number, String imageUrl, String setName, int printedTotal) {
        this.id = id;
        this.name = name;
        this.number = number;
        this.imageUrl = imageUrl;
        this.setName = setName;
        this.printedTotal = printedTotal;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getSetName() {
        return setName;
    }

    public int getPrintedTotal() {
        return printedTotal;
    }
}
