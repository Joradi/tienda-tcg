package com.tiendatcg.order;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class CheckoutStockException extends RuntimeException {
    public CheckoutStockException(String message) {
        super(message);
    }
}
