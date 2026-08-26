package com.tiendatcg.product;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public class ProductUpdateRequest {

    @PositiveOrZero
    private Integer stock;

    @Positive
    private Long price;

    public ProductUpdateRequest() {
    }

    public Integer getStock() {
        return stock;
    }

    public Long getPrice() {
        return price;
    }
}
