package com.tiendatcg.cart;

import jakarta.validation.constraints.Positive;

public class CartUpdateItemRequest {

    @Positive
    private int quantity;

    public CartUpdateItemRequest() {
    }

    public CartUpdateItemRequest(int quantity) {
        this.quantity = quantity;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
