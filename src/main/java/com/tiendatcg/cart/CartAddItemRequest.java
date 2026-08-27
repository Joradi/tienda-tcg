package com.tiendatcg.cart;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CartAddItemRequest {

    @NotNull
    private Long productId;

    @Positive
    private int quantity;

    public CartAddItemRequest() {
    }

    public CartAddItemRequest(int quantity) {
        this.quantity = quantity;
    }

    public Long getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }
}
