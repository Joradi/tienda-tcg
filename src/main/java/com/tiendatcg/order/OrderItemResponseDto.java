package com.tiendatcg.order;

public class OrderItemResponseDto {

    private Long productId;
    private String cardName;
    private String imageUrl;
    private String language;
    private String variant;
    private String condition;
    private long unitPrice;
    private int quantity;
    private long subtotal;

    public OrderItemResponseDto(Long productId, String cardName, String imageUrl, String language, String variant, String condition, long unitPrice, int quantity, long subtotal) {
        this.productId = productId;
        this.cardName = cardName;
        this.imageUrl = imageUrl;
        this.language = language;
        this.variant = variant;
        this.condition = condition;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.subtotal = subtotal;
    }

    public Long getProductId() {
        return productId;
    }

    public String getCardName() {
        return cardName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getLanguage() {
        return language;
    }

    public String getVariant() {
        return variant;
    }

    public String getCondition() {
        return condition;
    }

    public long getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public long getSubtotal() {
        return subtotal;
    }
}