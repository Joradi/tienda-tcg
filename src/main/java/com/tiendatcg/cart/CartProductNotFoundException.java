package com.tiendatcg.cart;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CartProductNotFoundException extends RuntimeException {
    public CartProductNotFoundException(String message) {
        super(message);
    }
}
