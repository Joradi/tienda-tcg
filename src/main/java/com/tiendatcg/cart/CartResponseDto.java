package com.tiendatcg.cart;

import java.util.List;
import java.util.UUID;

public class CartResponseDto {

    private Long cartId;
    private UUID guestToken;
    private List<CartItemResponseDto> items;
    private long netAmount;
    private long taxAmount;
    private long total;

    public CartResponseDto(
            Long cartId,
            UUID guestToken,
            List<CartItemResponseDto> items,
            long netAmount,
            long taxAmount,
            long total
    ) {
        this.cartId = cartId;
        this.guestToken = guestToken;
        this.items = items;
        this.netAmount = netAmount;
        this.taxAmount = taxAmount;
        this.total = total;
    }

    public Long getCartId() {
        return cartId;
    }

    public UUID getGuestToken() {
        return guestToken;
    }

    public List<CartItemResponseDto> getItems() {
        return items;
    }

    public long getNetAmount() {
        return netAmount;
    }

    public long getTaxAmount() {
        return taxAmount;
    }

    public long getTotal() {
        return total;
    }
}